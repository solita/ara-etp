<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as Kayttajat from '@Utility/kayttajat';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as Viestit from '@Component/viesti/viesti-util';
  import * as Schema from './schema';

  import { flashMessageStore, idTranslateStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Link from '@Component/Link/Link.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
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

  const load = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Response.notFound(response)
            ? `${i18nRoot}.messages.not-found`
            : Maybe.orSome(
                `${i18nRoot}.messages.load-error`,
                Response.localizationKey(response)
              )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some({
          whoami: response[0],
          ketju: response[1],
          ryhmat: response[2],
          kasittelijat: response[3]
        });
        overlay = false;
        idTranslateStore.updateKetju(response[1]);
      }
    ),
    Future.parallel(4),
    R.tap(enableOverlay),
    R.append(api.getKasittelijat),
    R.append(api.vastaanottajaryhmat),
    R.pair(kayttajaApi.whoami),
    api.ketju
  );

  $: load(params.id);

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
        flashMessageStore.add('viesti', 'update-error', msg);
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
      addNewViesti(newViesti);
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
</script>

<style>
  .message {
    @apply p-4 flex flex-col rounded-lg;
  }
  .message:not(.self) {
    @apply mr-8 bg-backgroundhalf;
  }
  .message.self {
    @apply ml-8 bg-light border-backgroundhalf border;
  }
  .message p {
    @apply border-disabled whitespace-pre-wrap mt-2 pt-2 border-t overflow-x-auto;
  }
</style>

<!-- purgecss: hidden -->

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each resources.toArray() as { ketju, whoami, ryhmat, kasittelijat }}
      <DirtyConfirmation {dirty} />
      <div class="flex">
        <Link
          text={i18n(i18nRoot + '.back')}
          href="#/viesti/all"
          icon={Maybe.Some('arrow_back')} />
      </div>

      <div
        class="flex justify-between items-center py-2 border-b border-backgroundhalf">
        <div>
          <strong>{ketju.subject}</strong>
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

        {#if !Kayttajat.isLaatija(whoami)}
          <div class="flex items-end space-x-4 px-2">
            <div class="flex flex-col">
              <label for="handler" class="text-secondary">
                {i18n(i18nRoot + '.handler')}
              </label>
              {#if R.prop('kasittelija-id', ketju)}
                <span name="handler" class="whitespace-no-wrap">
                  {R.compose(
                    R.join(' '),
                    R.props(['etunimi', 'sukunimi']),
                    R.find(R.propEq('id', R.prop('kasittelija-id', ketju)))
                  )(kasittelijat)}
                </span>
              {:else}
                <span name="handler" class="text-error whitespace-no-wrap">
                  {i18n(i18nRoot + '.no-handler')}
                </span>
              {/if}
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

      {#if Kayttajat.isLaatija(whoami) && Viestit.isForLaatijat(ketju)}
        <div class="font-bold my-4 mr-auto flex flex-shrink">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.reply-in-new')}
            href="#/viesti/new?subject={ketju.subject}" />
        </div>
      {/if}

      {#if (Kayttajat.isLaatija(whoami) && !Viestit.isForLaatijat(ketju)) || Kayttajat.isPaakayttaja(whoami)}
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
            <Textarea
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
            {#if (Kayttajat.isLaatija(whoami) && !Viestit.isForLaatijat(ketju)) || Kayttajat.isPaakayttaja(whoami)}
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
            <p>{viesti.body}</p>
          </div>
        {/each}
      </div>
    {/each}

    <div class="flex mt-4">
      <Link
        text={$_(i18nRoot + '.back')}
        href="#/viesti/all"
        icon={Maybe.Some('arrow_back')} />
    </div>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
