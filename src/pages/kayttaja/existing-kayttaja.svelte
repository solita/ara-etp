<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Laatija from '@Pages/laatija/laatija';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, idTranslateStore } from '@/stores';

  import * as GeoApi from '@Utility/api/geo-api';
  import * as LaatijaApi from '@Pages/laatija/laatija-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as LaskutusApi from '@Utility/api/laskutus-api';

  import AineistoasiakasForm from './aineistoasiakas-form.svelte';
  import KayttajaForm from './kayttaja-form.svelte';
  import LaatijaForm from '@Pages/laatija/laatija-form.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import H1 from '@Component/H/H1.svelte';
  import LastLogin from './last-login.svelte';
  import Verification from './verification.svelte';

  export let params;

  const i18n = $_;
  const i18nRoot = 'kayttaja';

  let resources = Maybe.None();
  let overlay = true;
  let dirty = false;

  const errorMessage = (type, response) =>
    Locales.uniqueViolationMessage(
      i18n,
      response,
      Response.errorKey(type, 'save', response)
    );

  const fork = (type, putFuture, onSuccessfulSave) => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        flashMessageStore.add(
          'kayttaja',
          'error',
          errorMessage(type, response)
        );
      },
      _ => {
        flashMessageStore.add(
          'kayttaja',
          'success',
          i18n(`${type}.messages.save-success`)
        );
        load(params);
        onSuccessfulSave();
        overlay = false;
        dirty = false;
      },
      putFuture
    );
  };

  const submitLaatija = (whoami, id) => updatedLaatija =>
    fork(
      'laatija',
      LaatijaApi.putLaatijaById(
        whoami.rooli,
        fetch,
        id,
        Laatija.fromLaatijaForm(updatedLaatija)
      ),
      _ => {}
    );

  const submitKayttaja = (whoami, id) => updatedKayttaja =>
    fork(
      'kayttaja',
      KayttajaApi.putKayttajaById(whoami.rooli, fetch, id, updatedKayttaja),
      _ => {
        idTranslateStore.updateKayttaja(updatedKayttaja);
      }
    );

  const submitAineistoasiakas = (whoami, id) => (
    updatedKayttaja,
    updatedKayttajaAineistot
  ) =>
    fork(
      'kayttaja',
      Future.parallelObject(2, {
        kayttaja: KayttajaApi.putKayttajaById(
          whoami.rooli,
          fetch,
          id,
          updatedKayttaja
        ),
        aineistot: KayttajaApi.putKayttajaAineistot(
          fetch,
          id,
          updatedKayttajaAineistot
        )
      }),
      _ => {
        idTranslateStore.updateKayttaja(updatedKayttaja);
      }
    );

  const load = params => {
    overlay = true;
    resources = Maybe.None();
    const kayttajaFuture = Future.cache(KayttajaApi.getKayttajaById(params.id));
    Future.fork(
      response => {
        flashMessageStore.add(
          'kayttaja',
          'error',
          i18n(Response.errorKey404(i18nRoot, 'load', response))
        );
        resources = Maybe.None();
        overlay = false;
      },
      response => {
        idTranslateStore.updateKayttaja(
          R.assoc(
            'partner',
            Maybe.fold(false, R.prop('partner'), response.laatija),
            response.kayttaja
          )
        );
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
      },
      Future.parallelObject(7, {
        kayttaja: kayttajaFuture,
        laatija: R.chain(
          kayttaja =>
            Kayttajat.isLaatija(kayttaja)
              ? R.map(Maybe.Some, KayttajaApi.getLaatijaById(fetch, params.id))
              : Future.resolve(Maybe.None()),
          kayttajaFuture
        ),
        kayttajaAineistot: R.chain(
          ({ kayttaja, aineistot }) =>
            Kayttajat.isAineistoasiakas(kayttaja)
              ? KayttajaApi.getAineistotByKayttajaId(
                  aineistot,
                  fetch,
                  params.id
                )
              : Future.resolve([]),
          Future.parallelObject(2, {
            kayttaja: kayttajaFuture,
            aineistot: KayttajaApi.aineistot
          })
        ),
        whoami: KayttajaApi.whoami,
        roolit: KayttajaApi.roolit,
        aineistot: KayttajaApi.aineistot,
        luokittelut: Future.parallelObject(5, {
          countries: GeoApi.countries,
          toimintaalueet: GeoApi.toimintaalueet,
          patevyydet: LaatijaApi.patevyydet,
          laskutuskielet: LaskutusApi.laskutuskielet
        })
      })
    );
  };

  const mergeKayttajaLaatija = (kayttaja, laatija) =>
    R.compose(
      Laatija.toLaatijaForm,
      R.omit(['kayttaja', 'cognitoid', 'ensitallennus', 'virtu']),
      R.mergeRight
    )(kayttaja, R.omit(['henkilotunnus'], laatija));

  $: load(params);
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each resources.toArray() as { kayttaja, laatija, whoami, luokittelut, roolit, aineistot, kayttajaAineistot }}
      {#if Maybe.isSome(laatija)}
        <div class="mt-6">
          <LastLogin {kayttaja} />
          <Verification {whoami} {kayttaja} />
          <LaatijaForm
            submit={submitLaatija(whoami, params.id)}
            cancel={_ => load(params)}
            bind:dirty
            {whoami}
            {luokittelut}
            laatija={mergeKayttajaLaatija(kayttaja, Maybe.get(laatija))} />
        </div>
      {:else if Kayttajat.isAineistoasiakas(kayttaja)}
        <H1
          text={kayttaja.etunimi +
            ' ' +
            kayttaja.sukunimi +
            ' (' +
            kayttaja.organisaatio +
            ')'} />
        <AineistoasiakasForm
          submit={submitAineistoasiakas(whoami, params.id)}
          cancel={_ => load(params)}
          bind:dirty
          kayttaja={R.evolve({ rooli: Maybe.fromNull }, kayttaja)}
          {aineistot}
          {kayttajaAineistot}
          {whoami} />
      {:else}
        <H1 text={kayttaja.etunimi + ' ' + kayttaja.sukunimi} />
        <LastLogin {kayttaja} />
        <KayttajaForm
          submit={submitKayttaja(whoami, params.id)}
          cancel={_ => load(params)}
          bind:dirty
          kayttaja={R.evolve({ rooli: Maybe.fromNull }, kayttaja)}
          {whoami}
          {roolit} />
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
