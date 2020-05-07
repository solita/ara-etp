<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import {
    flashMessageStore,
    breadcrumbStore,
    currentUserStore
  } from '@/stores';

  import * as api from './kayttaja-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import * as laatijaApi from '@Component/Laatija/laatija-api';

  import * as LaatijaUtils from '@Component/Laatija/laatija-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import KayttajaForm from './KayttajaForm.svelte';
  import LaatijaForm from '@Component/Laatija/LaatijaForm.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  let kayttaja = Maybe.None();
  let laatija = Maybe.None();
  let luokittelut = Maybe.None();
  let overlay = true;

  const toggleOverlay = value => () => (overlay = value);

  $: submit = R.compose(
    Future.forkBothDiscardFirst(
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Kayttaja', 'error'),
        R.always($_('laatija.messages.save-error'))
      ),
      R.compose(
        R.tap(toggleOverlay(false)),
        flashMessageStore.add('Kayttaja', 'success'),
        R.always($_('laatija.messages.save-success'))
      )
    ),
    Future.delay(500),
    LaatijaUtils.putLaatijaByIdFuture(
      R.compose(
        Maybe.orSome(0),
        R.map(R.prop('rooli'))
      )($currentUserStore),
      fetch,
      params.id
    ),
    R.tap(toggleOverlay(true))
  );

  $: R.compose(
    Future.fork(
      R.compose(
        flashMessageStore.add('Kayttaja', 'error'),
        R.always($_('kayttaja.messages.load-error')),
        R.tap(toggleOverlay(false))
      ),
      R.compose(
        ([fetchedKayttaja, fetchedLaatija, fetchedLuokittelut]) => {
          kayttaja = Maybe.Some(fetchedKayttaja);
          laatija = Maybe.fromNull(fetchedLaatija);
          luokittelut = Maybe.Some(fetchedLuokittelut);
        },
        R.tap(toggleOverlay(false))
      )
    ),
    Future.parallel(5),
    R.append(Future.parallelObject(5, {
      countries: geoApi.countries,
      toimintaalueet: geoApi.toimintaalueet,
      patevyydet: laatijaApi.patevyydet})),
    R.juxt([api.getKayttajaById(fetch), api.getLaatijaById(fetch)]),
    R.prop('id')
  )(params);

  const mergeKayttajaLaatija = (kayttaja, laatija) =>
    R.compose(
      R.omit(['kayttaja', 'cognitoid', 'ensitallennus']),
      R.mergeRight)(kayttaja, laatija);

  // TODO: remove breadcrumb code here - see AE-205
  $: breadcrumbStore.set([
    {
      label: $_('kayttaja.omattiedot'),
      url: location.href
    }
  ]);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if Maybe.isSome(laatija)}
      <LaatijaForm {submit}
                   luokittelut={Maybe.get(luokittelut)}
                   laatija={mergeKayttajaLaatija(Maybe.get(kayttaja), Maybe.get(laatija))} />
    {:else if Maybe.isSome(kayttaja)}
      <KayttajaForm {submit} kayttaja={Maybe.get(kayttaja)} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
