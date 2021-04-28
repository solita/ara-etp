<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import { flashMessageStore } from '@/stores';
  import * as Router from '@Component/Router/router';

  import * as Links from './links';

  import * as ValvontaApi from './valvonta-api';
  import * as KayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import ToimenpideForm from './toimenpide-form.svelte';

  export let params;

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.toimenpide';

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;

  const errorMessage = (key, response) => i18n(
    Response.notFound(response)
      ? `${i18nRoot}.messages.not-found`
      : Maybe.orSome(`${i18nRoot}.messages.${key}-error`,
      Response.localizationKey(response)));

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        const msg = errorMessage('load', response);
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
      },
      Future.parallelObject(3, {
        whoami: KayttajaApi.whoami,
        templatesByType: ValvontaApi.templatesByType,
        toimenpide: ValvontaApi.toimenpide(params.id, params['toimenpide-id'])
      })
    );
  };

  $: load(params);

  const fork = (key, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        const msg = errorMessage(key, response);
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        successCallback();
      },
      future);
  };

  $: saveToimenpide = R.compose(
    fork('save', _ => load(params)),
    ValvontaApi.putToimenpide(params.id, params['toimenpide-id'])
  );

  $: saveAndPublishToimenpide = R.compose(
    fork('publish', _ => {
      dirty = false;
      Router.push(Links.valvonta(params));
    }),
    Future.and(ValvontaApi.publishToimenpide(params.id, params['toimenpide-id'])),
    ValvontaApi.putToimenpide(params.id, params['toimenpide-id'])
  );

  const cancel = _ => {
    load(params);
  };
</script>

<Overlay {overlay}>
  <slot />
  <div slot="content" class="w-full mt-3">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { whoami, templatesByType, toimenpide }}
      <ToimenpideForm
        {toimenpide}
        {templatesByType}
        bind:dirty
        {whoami}
        {cancel}
        submit={saveToimenpide}
        publish={Maybe.Some(saveAndPublishToimenpide)} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
