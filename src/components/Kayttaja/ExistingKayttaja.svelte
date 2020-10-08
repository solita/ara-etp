<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import {
    flashMessageStore,
    currentUserStore,
    idTranslateStore
  } from '@/stores';

  import * as api from './kayttaja-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as laskutusApi from '@Component/Laskutus/laskutus-api';

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

  const toggleOverlay = value => { overlay = value };

  const errorMessage = (type, response) =>
    R.ifElse(
      R.equals('unique-violation'),
      R.always(`unique-violations.${response.body.constraint}`),
      R.always(`${type}.messages.save-error`)
    )(response.body.type);

  const fork = (type, updatedModel) =>
    Future.fork(
      response => {
        toggleOverlay(false);
        flashMessageStore.add('Kayttaja', 'error', $_(errorMessage(type, response)));
      },
      _ => {
        flashMessageStore.add('Kayttaja', 'success', $_(`${type}.messages.save-success`));
        if (R.equals('kayttaja', type)) {
          idTranslateStore.updateKayttaja(updatedModel);
        }
        toggleOverlay(false);
      }
    );

  $: submitLaatija = updatedLaatija =>
    R.compose(
      fork('laatija', updatedLaatija),
      R.tap(() => toggleOverlay(true)),
      laatijaApi.putLaatijaById(
        R.compose(
          Maybe.orSome(0),
          R.map(R.prop('rooli'))
        )($currentUserStore),
        fetch,
        params.id
      )
    )(updatedLaatija);

  $: submitKayttaja = updatedKayttaja =>
    R.compose(
      fork('kayttaja', updatedKayttaja),
      kayttajaApi.putKayttajaById(
        R.compose(
          Maybe.orSome(0),
          R.map(R.prop('rooli'))
        )($currentUserStore),
        fetch,
        params.id
      ),
      R.tap(() => toggleOverlay(true))
    )(updatedKayttaja);

  $: R.compose(
    Future.fork(
      _ => {
        flashMessageStore.add('Kayttaja', 'error', $_('kayttaja.messages.load-error'));
        toggleOverlay(false);
     },
     ([fetchedKayttaja, fetchedLaatija, fetchedLuokittelut]) => {
        idTranslateStore.updateKayttaja(fetchedKayttaja);
        kayttaja = Maybe.Some(fetchedKayttaja);
        laatija = Maybe.fromNull(fetchedLaatija);
        luokittelut = Maybe.Some(fetchedLuokittelut);
        toggleOverlay(false);
      }
    ),
    Future.parallel(5),
    R.append(
      Future.parallelObject(5, {
        countries: geoApi.countries,
        toimintaalueet: geoApi.toimintaalueet,
        patevyydet: laatijaApi.patevyydet,
        laskutuskielet: laskutusApi.laskutuskielet
      })
    ),
    R.juxt([
      kayttajaApi.getKayttajaById(fetch),
      kayttajaApi.getLaatijaById(fetch)
    ]),
    R.prop('id')
  )(params);

  const mergeKayttajaLaatija = (kayttaja, laatija) =>
    R.compose(
      R.omit(['kayttaja', 'cognitoid', 'ensitallennus', 'virtu']),
      R.mergeRight
    )(kayttaja, laatija);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#if Maybe.isSome(laatija)}
      <LaatijaForm
        submit={submitLaatija}
        luokittelut={Maybe.get(luokittelut)}
        laatija={mergeKayttajaLaatija(Maybe.get(kayttaja), Maybe.get(laatija))} />
    {:else if Maybe.isSome(kayttaja)}
      <KayttajaForm submit={submitKayttaja} kayttaja={Maybe.get(kayttaja)} />
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
