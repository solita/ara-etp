<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as validation from '@Utility/validation';
  import * as formats from '@Utility/formats';
  import * as parsers from '@Utility/parsers';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as valvontaApi from '@Pages/energiatodistus/valvonta-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import Confirm from '@Component/Confirm/Confirm';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';
  import * as Response from '@Utility/response';

  export let params;

  const i18n = $_;

  const emptyLiite = _ => ({ nimi: '', url: '' });

  let overlay = true;
  let failure = false;
  let enabled = false;
  let liitteet;
  let liiteLinkAdd;
  let files = [];
  let whoami = Maybe.None();

  let linkNimi = '';
  let linkUrl = '';
  let linkNimiValid = true;
  let linkUrlValid = true;

  const resetForm = () => {
    liitteet = [];
    liiteLinkAdd = emptyLiite();
  };

  const enableOverlay = _ => {
    overlay = true;
  };

  resetForm();

  const liiteLinkAddSchema = {
    nimi: [validation.isRequired, validation.maxLengthConstraint(300)],
    url: [validation.isRequired, validation.urlValidator]
  };

  const orEmpty = Maybe.orSome('');
  const cancel = _ => {
    linkNimi = '';
    linkUrl = '';
    linkNimiValid = true;
    linkUrlValid = true;
    liiteLinkAdd = emptyLiite();
  };

  const getValvonta = (_, id) => valvontaApi.getValvonta(fetch, id);

  const load = R.compose(
    Future.fork(
      _ => {
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          i18n('energiatodistus.liitteet.messages.load-error')
        );
        overlay = false;
      },
      response => {
        whoami = Maybe.Some(response[0]);
        liitteet = response[1];
        enabled = response[2].active;
        overlay = false;
      }
    ),
    R.tap(enableOverlay),
    Future.parallel(5),
    R.prepend(kayttajaApi.whoami),
    R.juxt([api.getLiitteetById(fetch), getValvonta])
  );

  const viewError = (functionName, response) => {
    const msg = i18n(
      Maybe.orSome(
        `energiatodistus.liitteet.messages.${functionName}.error`,
        Response.localizationKey(response)
      )
    );
    flashMessageStore.add('Energiatodistus', 'error', msg);
  };

  const fork = functionName =>
    Future.fork(
      response => {
        viewError(functionName, response);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'Energiatodistus',
          'success',
          i18n(`energiatodistus.liitteet.messages.${functionName}.success`)
        );
        load(params.version, params.id);
      }
    );

  $: files.length > 0 &&
    R.compose(
      fork('add-files'),
      R.tap(enableOverlay),
      api.postLiitteetFiles(fetch, params.version, params.id)
    )(files);

  const addLink = R.compose(
    fork('add-link'),
    R.tap(enableOverlay),
    api.postLiitteetLink(fetch, params.version, params.id)
  );

  const submit = event => {
    if (isValidForm(liiteLinkAdd)) {
      flashMessageStore.flush();
      addLink(liiteLinkAdd);
      resetForm();
    } else {
      flashMessageStore.add(
        'Energiatodistus',
        'error',
        i18n('energiatodistus.liitteet.messages.add-link.validation')
      );
    }
  };

  $: load(params.version, params.id);

  const liiteUrl = liite =>
    Maybe.orSome(
      api.url.liitteet(params.version, params.id) +
        '/' +
        liite.id +
        '/' +
        encodeURIComponent(liite.nimi),
      liite.url
    );

  const deleteLiite = R.compose(
    fork('delete-liite'),
    R.tap(enableOverlay),
    api.deleteLiite(fetch, params.version, params.id)
  );

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    validation.validateModelObject(liiteLinkAddSchema)
  );

  $: linkEmpty = linkNimi.length === 0 || linkUrl.length === 0;
  $: linkInvalid = !(linkNimiValid && linkUrlValid);

  const saveValvonta = event =>
    Future.fork(
      response => {
        viewError('valvonta', response);
        enabled = !enabled;
      },
      _ => {
        flashMessageStore.add(
          'Energiatodistus',
          'success',
          i18n(`energiatodistus.liitteet.messages.valvonta.success`)
        );
      },
      valvontaApi.putValvonta(fetch, params.id, enabled)
    );

  $: isDeleteEnabled =
    enabled || Maybe.fold(false, Kayttajat.isPaakayttaja, whoami);
</script>

<style>
  .delete-icon:hover:not(.text-disabled) {
    @apply text-error cursor-pointer;
  }

  .delete-icon.text-disabled {
    @apply cursor-not-allowed;
  }
</style>

<div class="w-full mt-3">
  <H1 text={i18n('energiatodistus.liitteet.title')} />

  <Overlay {overlay}>
    <div slot="content" class="mb-10">
      {#if Maybe.fold(false, Kayttajat.isPaakayttaja, whoami)}
        <div class="mb-5" on:change={saveValvonta}>
          <Checkbox
            bind:model={enabled}
            label={i18n('energiatodistus.liitteet.enabled-checkbox-label')} />
        </div>
      {/if}

      {#if !enabled}
        <div class="mb-5 bg-warning flex p-5">
          <span class="font-icon mr-2">warning</span>
          {i18n('energiatodistus.liitteet.attachments-disabled')}
        </div>
      {/if}

      {#if R.isEmpty(liitteet)}
        <p>{i18n('energiatodistus.liitteet.empty')}</p>
      {:else}
        <div class="overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr">
                <th class="etp-table--th">
                  {i18n('energiatodistus.liitteet.liite.createtime')}
                </th>
                <th class="etp-table--th">
                  {i18n('energiatodistus.liitteet.liite.author')}
                </th>
                <th class="etp-table--th">
                  {i18n('energiatodistus.liitteet.liite.nimi')}
                </th>
                <th class="etp-table--th">
                  {i18n('energiatodistus.liitteet.liite.type')}
                </th>
                <th class="etp-table--th etp-table--th__center">
                  <span class="material-icons">delete_forever</span>
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each liitteet as liite}
                <tr class="etp-table--tr">
                  <td class="etp-table--td">
                    {formats.formatTimeInstant(liite.createtime)}
                  </td>
                  <td class="etp-table--td">{liite['author-fullname']}</td>
                  <td class="etp-table--td">
                    <a
                      class="hover:underline font-bold text-link"
                      target="_self"
                      href={liiteUrl(liite)}>
                      {liite.nimi}
                    </a>
                  </td>
                  <td class="etp-table--td etp-table--td__center">
                    <span
                      class="material-icons cursor-default"
                      title={liite['contenttype']}>
                      {Maybe.isSome(liite.url) ? 'link' : 'attachment'}
                    </span>
                  </td>
                  <td class="etp-table--td etp-table--td__center">
                    <Confirm
                      let:confirm
                      confirmButtonLabel={i18n('confirm.button.delete')}
                      confirmMessage={i18n('confirm.you-want-to-delete')}>
                      <span
                        class="material-icons delete-icon"
                        class:text-disabled={!isDeleteEnabled}
                        title={!isDeleteEnabled
                          ? i18n('energiatodistus.liitteet.poista-disabled')
                          : ''}
                        on:click|stopPropagation={_ => {
                          if (isDeleteEnabled) confirm(deleteLiite, liite.id);
                        }}>
                        highlight_off
                      </span>
                    </Confirm>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>

  {#if enabled}
    <div class="mb-4 flex lg:flex-row flex-col">
      <div class="lg:w-1/2 w-full mr-6 mb-6">
        <div class="flex space-x-1 text-primary">
          <span class="material-icons"> attachment </span>
          <H2 text={i18n('energiatodistus.liitteet.add-files.title')} />
        </div>
        <FileDropArea bind:files multiple={true} />
      </div>
      <div class="lg:w-1/2 w-full flex flex-col">
        <div class="flex space-x-1 text-primary">
          <span class="material-icons"> link </span>
          <H2 text={i18n('energiatodistus.liitteet.add-link.title')} />
        </div>
        <form on:submit|preventDefault={submit}>
          <div class="w-full py-4">
            <Input
              id={'link.url'}
              name={'link.url'}
              label={i18n('energiatodistus.liitteet.add-link.url')}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['url'])}
              bind:currentValue={linkUrl}
              bind:valid={linkUrlValid}
              required={true}
              parse={R.compose(parsers.addDefaultProtocol, R.trim)}
              validators={liiteLinkAddSchema.url}
              {i18n} />
          </div>
          <div class="w-full py-4">
            <Input
              id={'link.nimi'}
              name={'link.nimi'}
              label={i18n('energiatodistus.liitteet.add-link.nimi')}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['nimi'])}
              bind:currentValue={linkNimi}
              bind:valid={linkNimiValid}
              required={true}
              parse={R.trim}
              validators={liiteLinkAddSchema.nimi}
              {i18n} />
          </div>

          <div class="flex space-x-4 pt-8">
            <Button
              disabled={linkEmpty || linkInvalid}
              type={'submit'}
              text={'Lisää linkki'} />
            <Button
              on:click={cancel}
              text={'Tyhjennä'}
              type={'reset'}
              style={'secondary'} />
          </div>
        </form>
      </div>
    </div>
  {/if}
</div>
