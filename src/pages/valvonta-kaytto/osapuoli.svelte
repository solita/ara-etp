<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Objects from '@Utility/objects';
  import * as Links from './links';

  import { push } from '@Component/Router/router';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import HenkiloForm from './henkilo-form.svelte';
  import YritysForm from './yritys-form.svelte';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as GeoApi from '@Component/Geo/geo-api';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';

  export let params;
  export let type;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';
  const types = {
    henkilo: {
      name: henkilo => `${henkilo.etunimi} ${henkilo.sukunimi}`,
      form: HenkiloForm,
      get: ValvontaApi.getHenkilo,
      put: ValvontaApi.putHenkilo,
      delete: ValvontaApi.deleteHenkilo
    },
    yritys: {
      name: yritys => yritys.nimi,
      form: YritysForm,
      get: ValvontaApi.getYritys,
      put: ValvontaApi.putYritys,
      delete: ValvontaApi.deleteYritys
    }
  }

  let overlay = true;
  let dirty = false;

  let resources = Maybe.None();

  const load = (osapuoliType, params) => {
    overlay = true;
    Future.fork(
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
        osapuoli: osapuoliType.get(params.id, params['valvonta-id']),
        roolit: ValvontaApi.roolit,
        toimitustavat: ValvontaApi.toimitustavat,
        countries: GeoApi.countries
      })
    );
  };

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

  const updateOsapuoli = osapuoliApi => R.compose(
    fork('save', _ => load(osapuoliApi, params)),
    osapuoliApi.put(params['valvonta-id'], params.id)
  );

  const deleteOsapuoli = osapuoliApi => R.compose(
    fork('delete', _ => push(Links.kohde({id: params['valvonta-id']}))),
    osapuoliApi.delete(params['valvonta-id'])
  );

  $: osapuoliType = Objects.requireNotNil(types[type],
    "Unsupported osapuolitype: " + type);
  $: load(osapuoliType, params);
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { osapuoli, roolit, toimitustavat, countries }}
      <H1 text={osapuoliType.name(osapuoli)} />
      <svelte:component this={osapuoliType.form}
        {osapuoli}
        {roolit}
        {toimitustavat}
        {countries}
        save={updateOsapuoli(osapuoliType)}
        revert={_ => load(osapuoliType, params)}
        remove={Maybe.Some(deleteOsapuoli(osapuoliType))}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
