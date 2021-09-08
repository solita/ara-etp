<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import { push } from '@Component/Router/router';
  import * as Links from './links';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import HenkiloForm from './henkilo-form.svelte';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as api from './valvonta-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';

  export let params;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuolet';

  let overlay = true;
  let dirty = false;

  let resources = Maybe.None();

  const load = params => Future.fork(
    response => {
      const msg = $_(
        `${i18nRoot}.messages.load-error`,
        Response.localizationKey(response)
      );

      flashMessageStore.add('valvonta-kaytto', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(4, {
      henkilo: api.getHenkilo(params.id, params['valvonta-id']),
      roolit: api.roolit,
      toimitustavat: api.toimitustavat,
      countries: geoApi.countries
    })
  );

  const fork = (key, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.${key}-error`,
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('valvonta-kaytto', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'valvonta-kaytto',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        dirty = false;
        overlay = false;
        successCallback();
      },
      future
    );
  };

  const updateHenkilo = R.compose(
    fork('save', _ => load(params)),
    ValvontaApi.putHenkilo(params['valvonta-id'], params.id)
  );

  const deleteHenkilo = R.compose(
    fork('delete', _ => push(Links.kohde({id: params['valvonta-id']}))),
    ValvontaApi.deleteHenkilo(params['valvonta-id'])
  );

  $: load(params);
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { henkilo, roolit, toimitustavat, countries }}
      <H1 text={`${henkilo.etunimi} ${henkilo.sukunimi}`} />
      <HenkiloForm
        {henkilo}
        {roolit}
        {toimitustavat}
        {countries}
        save={updateHenkilo}
        revert={_ => load(params)}
        remove={Maybe.Some(deleteHenkilo)}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
