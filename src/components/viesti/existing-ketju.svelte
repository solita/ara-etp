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

  import { flashMessageStore } from '@/stores';
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
        const msg = $_(Response.notFound(response) ?
          `${i18nRoot}.messages.not-found` :
          Maybe.orSome(
            `${i18nRoot}.messages.load-error`,
            Response.localizationKey(response)));

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some({
          whoami: response[0],
          ketju: response[1]
        });
        overlay = false;
      }),
    Future.parallel(2),
    R.tap(enableOverlay),
    R.pair(kayttajaApi.whoami),
    api.getKetju(fetch)
  );

  $: load(params.id);

  let newViesti = '';

  $: addNewViesti = R.compose(
    Future.fork(
      response => {
        const msg = $_(Maybe.orSome(
          `${i18nRoot}.messages.error`,
          Response.localizationKey(response)));
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add('viesti', 'success',
          $_(`${i18nRoot}.messages.success`));
        load(params.id);
        newViesti = '';
        dirty = false;
      }
    ),
    api.postNewViesti(fetch, params.id));

  const isValidForm = Validation.validateModelValue(Schema.ketju.body);

  const submitNewViesti = event => {
    if (isValidForm(newViesti).isRight()) {
      addNewViesti(newViesti);
    } else {
      flashMessageStore.add('viesti', 'error',
        $_(`${i18nRoot}.messages.validation-error`));
      Validation.blurForm(event.target);
    }
  }

  const cancel = _ => {
    newViesti = '';
    dirty = false;
  };

  $: formatSender = Viestit.formatSender($_);
</script>

<style>

</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each resources.toArray() as {ketju, whoami}}
      <H1 text={$_(`${i18nRoot}.title`) + ' - ' + ketju.subject}/>

      <DirtyConfirmation {dirty} />

      <div class="flex mb-4">
      <Link text={'\u2B05 Kaikki viestit'} href="#/viesti/all" />
      </div>

      <div class="divide-y-2 divide-hr">
        {#each ketju.viestit as viesti}
          <div class="py-2">
            <p>{formatSender(viesti.from)}</p>
            <p>{Formats.formatTimeInstant(viesti.senttime)}</p>
            <p>{viesti.body}</p>
          </div>
        {/each}
      </div>

      <form class="mt-5" on:submit|preventDefault={submitNewViesti}
            on:input={_ => { dirty = true; }}
            on:change={_ => { dirty = true; }}>
        <div class="w-full py-4">
          <Textarea
              id={'ketju.new-viesti'}
              name={'ketju.new-viesti'}
              label={$_(i18nRoot + '.new-viesti')}
              bind:model={newViesti}
              lens={R.lens(R.identity, R.identity)}
              required={true}
              parse={R.trim}
              validators={Schema.ketju.body}
              i18n={$_}/>
        </div>

        <div class="flex space-x-4 pt-8">
          <Button
              disabled={!dirty}
              type={'submit'}
              text={'Vastaa'}/>
          <Button
              on:click={cancel}
              disabled={!dirty}
              text={'Tyhjennä'}
              type={'reset'}
              style={'secondary'}/>
        </div>
      </form>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
