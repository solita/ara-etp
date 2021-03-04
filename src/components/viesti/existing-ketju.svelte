<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as Viestit from '@Component/viesti/viesti-util';
  import * as Schema from './schema';

  import { flashMessageStore, idTranslateStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import { push } from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Link from '@Component/Link/Link.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  const i18nRoot = 'viesti.ketju.existing';

  export let params;

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };

  const load = R.compose(
    Future.fork(
      response => {
        const msg = $_(
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
          ketju: response[1]
        });
        overlay = false;
        idTranslateStore.updateKetju(response[1]);
      }
    ),
    Future.parallel(2),
    R.tap(enableOverlay),
    R.pair(kayttajaApi.whoami),
    api.ketju
  );

  $: load(params.id);

  let newViesti = '';

  $: addNewViesti = R.compose(
    Future.fork(
      response => {
        const msg = $_(
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
          $_(`${i18nRoot}.messages.success`)
        );
        load(params.id);
        newViesti = '';
        dirty = false;
      }
    ),
    api.postNewViesti(fetch, params.id)
  );

  const isValidForm = Validation.validateModelValue(Schema.ketju.body);

  const submitNewViesti = event => {
    if (isValidForm(newViesti).isRight()) {
      addNewViesti(newViesti);
    } else {
      flashMessageStore.add(
        'viesti',
        'error',
        $_(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const cancel = _ => {
    newViesti = '';
    dirty = false;
  };

  $: formatSender = Viestit.formatSender($_);
</script>

<style>
  .message {
    @apply p-4 flex flex-col rounded-lg;
  }
  .message:not(.self) {
    @apply mr-8 bg-background;
  }
  .message.self {
    @apply ml-8 bg-light border-tableborder border;
  }
  .message.self .from-me {
    @apply text-primary block;
  }
  .message:not(.self) .from {
    @apply block;
  }
  .message p {
    @apply border-tableborder whitespace-pre-wrap mt-2 pt-2 border-t overflow-x-auto;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each resources.toArray() as { ketju, whoami }}
      <DirtyConfirmation {dirty} />

      <H1 text={$_(`${i18nRoot}.title`) + ' - ' + ketju.subject} />

      <div class="space-y-6">
        {#each ketju.viestit as viesti}
          <div
            class="message"
            class:self={R.propEq('id', R.path(['from', 'id'], viesti), whoami)}>
            <strong class="from hidden">{formatSender(viesti.from)}</strong>
            <strong class="from-me hidden">{$_(i18nRoot + '.self')}</strong>
            <span class="italic text-sm">
              {Formats.formatTimeInstant(viesti['sent-time'])}
            </span>
            <p>{viesti.body}</p>
          </div>
        {/each}
      </div>

      <form
        class="p-4 mt-6 ml-8 rounded-lg border-tableborder border"
        on:submit|preventDefault={submitNewViesti}
        on:input={_ => {
          dirty = true;
        }}
        on:change={_ => {
          dirty = true;
        }}>
        <div class="w-full mb-8 space-y-2">
          <strong>{$_(i18nRoot + '.new-viesti')}</strong>
          <Textarea
            id={'ketju.new-viesti'}
            name={'ketju.new-viesti'}
            bind:model={newViesti}
            lens={R.lens(R.identity, R.identity)}
            required={true}
            parse={R.trim}
            compact={true}
            validators={Schema.ketju.body}
            i18n={$_} />
        </div>

        <div class="w-full flex space-x-4">
          <Button
            disabled={!dirty}
            type={'submit'}
            text={$_(i18nRoot + '.submit')} />
        </div>
      </form>
    {/each}

    <div class="flex mt-16">
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
