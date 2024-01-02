<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';

  import { _ } from '@Language/i18n';
  import { announcementsForModule } from '@Utility/announce';
  import { push } from '@Component/Router/router';

  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as Kayttajat from '@Utility/kayttajat';

  import AineistoasiakasForm from '@Pages/kayttaja/aineistoasiakas-form.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import H1 from '@Component/H/H1.svelte';

  const i18n = $_;
  const i18nRoot = 'aineistoasiakas.new';
  const { announceError, announceSuccess } =
    announcementsForModule('aineistoasiakas');

  let resources = Maybe.None();
  const emptyKayttaja = {
    login: Maybe.None(),
    passivoitu: false,
    rooli: Maybe.Some(Kayttajat.role.aineistoasiakas),
    etunimi: '',
    sukunimi: '',
    email: '',
    puhelin: '',
    virtu: Maybe.None(),
    henkilotunnus: Maybe.None(),
    organisaatio: '',
    'api-key': Maybe.None()
  };

  let kayttaja = emptyKayttaja;
  let kayttajaAineistot = [];
  let overlay = true;
  let dirty = false;

  const submitAineistot = ({ id }) =>
    R.map(
      R.always({ id }),
      KayttajaApi.putKayttajaAineistot(fetch, id, kayttajaAineistot)
    );

  const submit = kayttaja => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        announceError(
          Locales.uniqueViolationMessage(
            i18n,
            response,
            Response.errorKey(i18nRoot, 'add', response)
          )
        );
      },
      response => {
        announceSuccess(i18n(`${i18nRoot}.messages.add-success`));
        overlay = false;
        dirty = false;
        push('/kayttaja/' + response.id);
      },
      R.chain(submitAineistot, KayttajaApi.postKayttaja(kayttaja))
    );
  };

  const aineistotFuture = KayttajaApi.aineistot;

  $: Future.fork(
    response => {
      announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
      resources = Maybe.None();
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(3, {
      whoami: KayttajaApi.whoami,
      roolit: KayttajaApi.roolit,
      aineistot: aineistotFuture
    })
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    <H1 text={i18n(i18nRoot + '.title')} />
    {#each resources.toArray() as { whoami, roolit, aineistot, emptyAineistot }}
      <AineistoasiakasForm
        {submit}
        cancel={_ => {
          kayttaja = emptyKayttaja;
          kayttajaAineistot = emptyAineistot;
        }}
        bind:dirty
        {kayttaja}
        {whoami}
        {roolit}
        {aineistot}
        bind:kayttajaAineistot />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
