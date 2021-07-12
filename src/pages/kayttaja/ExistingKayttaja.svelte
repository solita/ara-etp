<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';
  import { flashMessageStore, idTranslateStore } from '@/stores';

  import * as geoApi from '@Component/Geo/geo-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laskutusApi from '@Component/Laskutus/laskutus-api';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import KayttajaForm from './KayttajaForm.svelte';
  import LaatijaForm from '@Pages/laatija/LaatijaForm.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  const i18n = $_;

  let resources = Maybe.None();
  let overlay = true;

  const toggleOverlay = value => {
    overlay = value;
  };

  const errorMessage = (type, response) =>
    Locales.uniqueViolationMessage(
      i18n,
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
          i18n(errorMessage(type, response))
        );
      },
      _ => {
        flashMessageStore.add(
          'Kayttaja',
          'success',
          i18n(`${type}.messages.save-success`)
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
      const msg = Response.notFound(response)
        ? i18n('kayttaja.messages.not-found')
        : i18n(
            Maybe.orSome(
              'kayttaja.messages.load-error',
              Response.localizationKey(response)
            )
          );

      flashMessageStore.add('Kayttaja', 'error', msg);
      resources = Maybe.None();
      toggleOverlay(false);
    },
    response => {
      idTranslateStore.updateKayttaja(response.kayttaja);
      resources = Maybe.Some(response);
      toggleOverlay(false);
    },
    Future.parallelObject(5, {
      kayttaja: kayttajaApi.getKayttajaById(params.id),
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
        <KayttajaForm
          submit={submitKayttaja(whoami, params.id)}
          {kayttaja}
          {whoami} />
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
