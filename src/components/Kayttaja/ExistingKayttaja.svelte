<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';
  import { flashMessageStore, idTranslateStore } from '@/stores';

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

  let resources = Maybe.None();
  let overlay = true;

  const toggleOverlay = value => {
    overlay = value;
  };

  const errorMessage = (type, response) =>
    Locales.uniqueViolationMessage(
      $_,
      response,
      Maybe.orSome(
        `${type}.messages.save-error`,
        Response.localizationKey(response)
      )
    );

  const fork = (type, updatedModel) =>
    Future.fork(
      response => {
        toggleOverlay(false);
        flashMessageStore.add(
          'Kayttaja',
          'error',
          $_(errorMessage(type, response))
        );
      },
      _ => {
        flashMessageStore.add(
          'Kayttaja',
          'success',
          $_(`${type}.messages.save-success`)
        );
        if (R.equals('kayttaja', type)) {
          idTranslateStore.updateKayttaja(updatedModel);
        }
        toggleOverlay(false);
      }
    );

  const submitLaatija = (whoami, id) => updatedLaatija =>
    R.compose(
      fork('laatija', updatedLaatija),
      R.tap(() => toggleOverlay(true)),
      laatijaApi.putLaatijaById(whoami.rooli, fetch, id)
    )(updatedLaatija);

  const submitKayttaja = (whoami, id) => updatedKayttaja =>
    R.compose(
      fork('kayttaja', updatedKayttaja),
      R.tap(() => toggleOverlay(true)),
      kayttajaApi.putKayttajaById(whoami.rooli, fetch, id)
    )(updatedKayttaja);

  $: Future.fork(
    response => {
      const msg = $_(
        Maybe.orSome(
          'kayttaja.messages.load-error',
          Response.localizationKey(response)
        )
      );
      flashMessageStore.add('Kayttaja', 'error', msg);
      toggleOverlay(false);
    },
    response => {
      idTranslateStore.updateKayttaja(response.kayttaja);
      resources = Maybe.Some(response);
      toggleOverlay(false);
    },
    Future.parallelObject(5, {
      kayttaja: kayttajaApi.getKayttajaById(fetch, params.id),
      laatija: R.map(
        Maybe.fromNull,
        kayttajaApi.getLaatijaById(fetch, params.id)
      ),
      whoami: kayttajaApi.whoami,
      luokittelut: Future.parallelObject(5, {
        countries: geoApi.countries,
        toimintaalueet: geoApi.toimintaalueet,
        patevyydet: laatijaApi.patevyydet,
        laskutuskielet: laskutusApi.laskutuskielet
      })
    })
  );

  const mergeKayttajaLaatija = (kayttaja, laatija) =>
    R.compose(
      R.omit(['kayttaja', 'cognitoid', 'ensitallennus', 'virtu']),
      R.mergeRight
    )(kayttaja, laatija);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each resources.toArray() as { kayttaja, laatija, whoami, luokittelut }}
      {#if Maybe.isSome(laatija)}
        <LaatijaForm
          submit={submitLaatija(whoami, params.id)}
          {whoami}
          {luokittelut}
          laatija={mergeKayttajaLaatija(kayttaja, Maybe.get(laatija))} />
      {:else}
        <KayttajaForm submit={submitKayttaja(whoami, params.id)} {kayttaja} />
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
