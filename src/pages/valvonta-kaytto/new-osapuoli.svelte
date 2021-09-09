<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Objects from '@Utility/objects';
  import * as Links from './links';
  import * as Osapuolet from './osapuolet';
  import { push } from '@Component/Router/router';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import HenkiloForm from './henkilo-form.svelte';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as GeoApi from '@Component/Geo/geo-api';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';
  import YritysForm from '@Pages/valvonta-kaytto/yritys-form';

  export let params;
  export let type;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';
  const types = {
    henkilo: {
      form: HenkiloForm,
      post: ValvontaApi.postHenkilo,
      empty: Osapuolet.emptyHenkilo,
      link: Links.henkilo,
      title: 'new-henkilo'
    },
    yritys: {
      form: YritysForm,
      post: ValvontaApi.postYritys,
      empty: Osapuolet.emptyYritys,
      link: Links.yritys,
      title: 'new-yritys'
    }
  }

  let overlay = true;
  let osapuoli = types[type].empty();
  let dirty = false;

  let resources = Maybe.None();

  Future.fork(
    response => {
      const msg = $_(
        `${i18nRoot}.messages.load-error`,
        Response.localizationKey(response)
      );

      flashMessageStore.add('henkilo', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(3, {
      roolit: ValvontaApi.roolit,
      toimitustavat: ValvontaApi.toimitustavat,
      countries: GeoApi.countries
    })
  );

  const addOsapuoli = osapuoliType => R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('henkilo', 'error', msg);
        overlay = false;
      },
      response => {
        flashMessageStore.add(
          'henkilo',
          'success',
          i18n(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        push(osapuoliType.link({id: params['valvonta-id']}, response));
      }
    ),
    osapuoliType.post(params['valvonta-id'])
  );

  $: osapuoliType = Objects.requireNotNil(types[type],
    "Unsupported osapuolitype: " + type);
</script>

<H1 text={i18n(i18nRoot + '.' + osapuoliType.title + '-title')} />

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { roolit, toimitustavat, countries }}
      <svelte:component this={osapuoliType.form}
        {osapuoli}
        {roolit}
        {toimitustavat}
        {countries}
        save={addOsapuoli(osapuoliType)}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
