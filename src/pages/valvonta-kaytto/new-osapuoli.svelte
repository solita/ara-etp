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

  import * as GeoApi from '@Utility/api/geo-api';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';
  import YritysForm from '@Pages/valvonta-kaytto/yritys-form';
  import { announcementsForModule } from '@Utility/announce';

  export let params;
  export let type;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuoli';
  const { announceError, announceSuccess } =
    announcementsForModule('valvonta-kaytto');

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
  };

  let overlay = true;
  let osapuoli = types[type].empty();
  let dirty = false;

  let resources = Maybe.None();

  Future.fork(
    response => {
      announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
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

  const addOsapuoli = osapuoliType =>
    R.compose(
      Future.fork(
        response => {
          announceError(i18n(Response.errorKey(i18nRoot, 'add', response)));
          overlay = false;
        },
        response => {
          announceSuccess(i18n(`${i18nRoot}.messages.add-success`));
          dirty = false;
          push(osapuoliType.link({ id: params['valvonta-id'] }, response));
        }
      ),
      osapuoliType.post(params['valvonta-id'])
    );

  $: osapuoliType = Objects.requireNotNil(
    types[type],
    'Unsupported osapuolitype: ' + type
  );
</script>

<H1 text={i18n(i18nRoot + '.' + osapuoliType.title + '-title')} />

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { roolit, toimitustavat, countries }}
      <svelte:component
        this={osapuoliType.form}
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
