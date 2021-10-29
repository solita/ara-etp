<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as MD from '@Component/text-editor/markdown';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as Viestit from '@Pages/viesti/viesti-util';
  import * as Schema from './schema';

  import { flashMessageStore, idTranslateStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import { pop } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton';
  import TextEditor from '@Component/text-editor/text-editor';
  import MDStyle from '@Component/text-editor/style.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import AttachEtDialog from '@Pages/viesti/attach-energiatodistus-dialog';
  import Link from '@Component/Link/Link.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Select from '@Component/Select/Select';
  import SenderRecipients from './sender-recipients.svelte';
  import User from './user.svelte';

  const i18nRoot = 'viesti.ketju.existing';
  const i18n = $_;

  export let params;

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };

  let showViestiForm = false;

  const load = id => {
    enableOverlay();
    Future.fork(
      response => {
        flashMessageStore.add('viesti', 'error',
          i18n(Response.errorKey404(i18nRoot, 'load', response)));
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        idTranslateStore.updateKetju(response.ketju, response.liitteet);
      },
      Future.parallelObject(5, {
        liitteet: api.liitteet(id),
        ketju: api.ketju(id),
        whoami: kayttajaApi.whoami,
        kasittelijat: api.getKasittelijat,
        ryhmat: api.vastaanottajaryhmat
      }));
  };

  load(params.id);

  let newViesti = '';

  const addNewViesti = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`${i18nRoot}.messages.success`)
        );
        load(params.id);
        newViesti = '';
        dirty = false;
        showViestiForm = false;
      }
    ),
    api.postNewViesti(fetch, params.id)
  );

  const submitKasitelty = kasitelty => {
    updateKetju({
      kasitelty: kasitelty
    });
  };

  const submitKasittelija = kasittelija => {
    updateKetju({
      'kasittelija-id': kasittelija
    });
  };

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`${i18nRoot}.messages.update-success`)
        );
        load(params.id);
      }
    ),
    R.tap(enableOverlay),
    api.putKetju(fetch, params.id)
  );

  const isValidForm = Validation.validateModelValue(Schema.ketju.body);

  const submitNewViesti = event => {
    if (isValidForm(newViesti).isRight()) {
      enableOverlay();
      addNewViesti(newViesti);
      Validation.unblurForm(event.target);
    } else {
      flashMessageStore.add(
        'viesti',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const isSenderSelf = (viesti, whoami) =>
    R.propEq('id', R.path(['from', 'id'], viesti), whoami);

  $: formatSender = Viestit.formatSender(i18n);

  let showAttachEtDialog = false;
</script>

<style>
  .message {
    @apply p-4 flex flex-col rounded-lg;
  }
  .message:not(.self) {
    @apply mr-8 bg-backgroundhalf;
  }
  .message.self {
    @apply ml-8 bg-light border-disabled border;
  }
  .message p {
    @apply border-disabled mt-2 pt-2 border-t overflow-x-auto;
  }
  h1 {
      @apply text-lg mb-2;
  }
</style>

<!-- purgecss: hidden -->

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each resources.toArray() as { ketju, whoami, ryhmat, kasittelijat }}
      <DirtyConfirmation {dirty} />

      <div
        class="flex justify-between items-center py-2 border-b border-disabled">
        <div>
          <h1>{ketju.subject}</h1>
          <SenderRecipients
            sender={R.prop('from', R.head(ketju.viestit))}
            icons="true"
            {whoami}
            recipients={R.prop('vastaanottajat', ketju)}
            recipientGroup={Viestit.findKetjuVastaanottajaryhma(
              ryhmat,
              ketju
            )} />
        </div>

        <div class="flex flex-col items-start">
          {#if Viestit.isKasittelija(whoami)}
            <div class="flex w-full justify-between px-2">
              {#each ketju['energiatodistus-id'].toArray() as etId}
                <div class="flex w-full mr-auto space-x-1">
                  <span>{i18n(i18nRoot + '.attach-to-et.attached-to')}</span>
                  <Link
                    bold={true}
                    href={'/#/energiatodistus/' + etId}
                    text={etId} />
                </div>
              {/each}
              {#if showAttachEtDialog}
                <AttachEtDialog
                  ketjuId={ketju.id}
                  energiatodistusId={ketju['energiatodistus-id']}
                  close={success => {
                    if (success === true) {
                      flashMessageStore.add(
                        'viesti',
                        'success',
                        i18n(
                          `${i18nRoot}.attach-to-et.messages.update-success`
                        )
                      );
                      load(params.id);
                    }
                    showAttachEtDialog = false;
                  }} />
              {/if}
              <div class="mt-auto ml-auto justify-self-end">
                <TextButton
                  on:click={() => {
                    showAttachEtDialog = true;
                  }}
                  icon="edit"
                  text={ketju['energiatodistus-id'].isSome()
                    ? i18n(i18nRoot + '.attach-to-et.button-edit')
                    : i18n(i18nRoot + '.attach-to-et.button-attach-to-et')}
                  style={'secondary'} />
              </div>
            </div>

            <div class="flex items-end space-x-4 px-2 w-full">
              <div class="w-64">
                <Select
                  id={'kasittelija'}
                  label={i18n(i18nRoot + '.handler')}
                  noneLabel={`${i18nRoot}.no-handler`}
                  compact={true}
                  required={false}
                  disabled={false}
                  allowNone={true}
                  bind:model={ketju}
                  on:change={event =>
                    submitKasittelija(parseInt(event.target.value))}
                  lens={R.lensProp('kasittelija-id')}
                  format={Viestit.fullName(kasittelijat)}
                  items={R.pluck('id', kasittelijat)} />
              </div>

              <button
                on:click={submitKasitelty(!ketju.kasitelty)}
                class="flex items-center space-x-1">
                {#if ketju.kasitelty}
                  <span class="material-icons text-primary"> check_box </span>
                {:else}
                  <span class="material-icons"> check_box_outline_blank </span>
                {/if}
                <span>{i18n(i18nRoot + '.handled')}</span>
              </button>
            </div>
          {/if}
        </div>
      </div>

      {#if Kayttajat.isLaatija(whoami) && Viestit.isForLaatijat(ketju)}
        <div class="font-bold my-4 mr-auto flex flex-shrink">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.reply-in-new')}
            href="#/viesti/new?subject={ketju.subject}" />
        </div>
      {/if}

      {#if (Kayttajat.isLaatija(whoami) && !Viestit.isForLaatijat(ketju)) || Kayttajat.isPaakayttaja(whoami) || Kayttajat.isLaskuttaja(whoami)}
        <form
          class="p-4 my-4 ml-8 rounded-lg border-backgroundhalf"
          class:hidden={!showViestiForm}
          on:submit|preventDefault={submitNewViesti}
          on:input={_ => {
            dirty = true;
          }}
          on:change={_ => {
            dirty = true;
          }}>
          <div class="w-full mb-8 space-y-2">
            <TextEditor
              id={'ketju.new-viesti'}
              name={'ketju.new-viesti'}
              label={i18n(i18nRoot + '.new-viesti')}
              bind:model={newViesti}
              lens={R.lens(R.identity, R.identity)}
              required={true}
              parse={R.trim}
              compact={false}
              validators={Schema.ketju.body}
              {i18n} />
          </div>

          <div class="w-full flex">
            {#if (Kayttajat.isLaatija(whoami) && !Viestit.isForLaatijat(ketju)) || Kayttajat.isPaakayttaja(whoami) || Kayttajat.isLaskuttaja(whoami)}
              <Button
                prefix={'viesti'}
                disabled={!dirty}
                type={'submit'}
                text={i18n(i18nRoot + '.submit')} />
            {/if}
          </div>
        </form>
        <div
          class="w-full p-4 my-4 ml-8 space-y-2"
          class:hidden={showViestiForm}>
          <Button
            prefix={'uusiviesti'}
            text={i18n(i18nRoot + '.new-viesti')}
            on:click={() => (showViestiForm = true)} />
        </div>
      {/if}

      <div class="space-y-6">
        {#each R.reverse(ketju.viestit) as viesti}
          <div
            class="message"
            data-cy="message"
            class:self={isSenderSelf(viesti, whoami)}>
            <div class="flex space-x-6">
              <span>
                {Formats.formatTimeInstantMinutes(viesti['sent-time'])}
              </span>
              <span>
                {ketju.subject}
              </span>
            </div>
            <User user={viesti.from} {whoami} />
            <p>
              <MDStyle>
                {@html MD.toHtml(viesti.body)}
              </MDStyle>
            </p>
          </div>
        {/each}
      </div>
    {/each}

    <div class="flex mt-4">
      <TextButton
        on:click={pop}
        icon="arrow_back"
        text={i18n(i18nRoot + '.back')}
        style={'secondary'} />
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
