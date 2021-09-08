<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Validation from '@Utility/validation';
  import * as formats from '@Utility/formats';
  import * as parsers from '@Utility/parsers';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Links from './links';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import H2 from '@Component/H/H2';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import Confirm from '@Component/Confirm/Confirm';

  export let liitteet = [];
  export let disabled = false;
  export let emptyMessageKey = i18nRoot + '.empty';

  export let liiteApi;
  export let flashModule;

  const i18nRoot = 'energiatodistus.liitteet';
  const i18n = $_;

  export let dirty = false;
  let newLink;
  let files = [];

  const resetForm = () => {
    dirty = false;
    files = [];
    newLink = Links.empty();
  };

  resetForm();

  const orEmpty = Maybe.orSome('');

  $: if (files.length > 0) {
    liiteApi.addFiles(files);
    files = [];
  }

  const submit = event => {
    if (isValidForm(newLink)) {
      flashMessageStore.flush();
      liiteApi.addLink(newLink);
      resetForm();
      Validation.unblurForm(event.target);
    } else {
      Validation.blurForm(event.target);
      flashMessageStore.add(
        flashModule,
        'error',
        i18n(i18nRoot + '.messages.add-link.validation')
      );
    }
  };

  const liiteUrl = liite =>
    Maybe.orSome(
      liiteApi.getUrl + '/' + liite.id + '/' + encodeURIComponent(liite.nimi),
      liite.url
    );

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    Validation.validateModelObject(Links.schema)
  );

  const setDirty = _ => {
    dirty = true;
  };
</script>

<style>
  .delete-icon:hover:not(.text-disabled) {
    @apply text-error cursor-pointer;
  }

  .delete-icon.text-disabled {
    @apply cursor-not-allowed;
  }
</style>

<div class="mb-5">
  {#if R.isEmpty(liitteet)}
    <p>{i18n(emptyMessageKey)}</p>
  {:else}
    <div class="overflow-x-auto">
      <table class="etp-table">
        <thead class="etp-table--thead">
          <tr class="etp-table--tr">
            <th class="etp-table--th">
              {i18n(i18nRoot + '.liite.createtime')}
            </th>
            <th class="etp-table--th">
              {i18n(i18nRoot + '.liite.author')}
            </th>
            <th class="etp-table--th">
              {i18n(i18nRoot + '.liite.nimi')}
            </th>
            <th class="etp-table--th">
              {i18n(i18nRoot + '.liite.type')}
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
                    class:text-disabled={disabled}
                    title={disabled ? i18n(i18nRoot + '.poista-disabled') : ''}
                    on:click|stopPropagation={_ => {
                      if (!disabled) confirm(liiteApi.deleteLiite, liite.id);
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

{#if !disabled && !R.isNil(liiteApi)}
  <div class="mb-4 flex lg:flex-row flex-col">
    <div class="lg:w-1/2 w-full mr-6 mb-6">
      <div class="flex space-x-1 text-primary">
        <span class="material-icons"> attachment </span>
        <H2 text={i18n(i18nRoot + '.add-files.title')} />
      </div>
      <FileDropArea bind:files multiple={true} />
    </div>
    <div class="lg:w-1/2 w-full flex flex-col">
      <div class="flex space-x-1 text-primary">
        <span class="material-icons"> link </span>
        <H2 text={i18n(i18nRoot + '.add-link.title')} />
      </div>
      <form
        on:submit|preventDefault={submit}
        on:input={setDirty}
        on:change={setDirty}>
        <div class="w-full py-4">
          <Input
            id={'link.url'}
            name={'link.url'}
            label={i18n(i18nRoot + '.add-link.url')}
            bind:model={newLink}
            lens={R.lensProp('url')}
            required={true}
            parse={R.compose(parsers.addDefaultProtocol, R.trim)}
            validators={Links.schema.url}
            {i18n} />
        </div>
        <div class="w-full py-4">
          <Input
            id={'link.nimi'}
            name={'link.nimi'}
            label={i18n(i18nRoot + '.add-link.nimi')}
            bind:model={newLink}
            lens={R.lensProp('nimi')}
            required={true}
            parse={R.trim}
            validators={Links.schema.nimi}
            {i18n} />
        </div>

        <div class="flex space-x-4 pt-8">
          <Button disabled={!dirty} type={'submit'} text={'Lisää linkki'} />
          <Button
            disabled={!dirty}
            on:click={resetForm}
            text={'Tyhjennä'}
            type={'reset'}
            style={'secondary'} />
        </div>
      </form>
    </div>
  </div>
{/if}
