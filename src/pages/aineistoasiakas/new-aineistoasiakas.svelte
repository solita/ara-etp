<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
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

  const submit = kayttaja => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        flashMessageStore.add(
          'kayttaja',
          'error',
          Locales.uniqueViolationMessage(
            i18n,
            response,
            Response.errorKey(i18nRoot, 'add', response)
          )
        );
      },
      response => {
        flashMessageStore.add(
          'kayttaja',
          'success',
          i18n(`${i18nRoot}.messages.add-success`)
        );
        overlay = false;
        dirty = false;
        push('/kayttaja/' + response.id);
      },
      KayttajaApi.postKayttaja(kayttaja)
    );
  };

  const aineistotFuture = KayttajaApi.aineistot;

  $: Future.fork(
    response => {
      flashMessageStore.add(
        'kayttaja',
        'error',
        i18n(Response.errorKey(i18nRoot, 'load', response))
      );
      resources = Maybe.None();
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
      dirty = false;
      if (kayttajaAineistot.length === 0) {
        kayttajaAineistot = response.emptyAineistot;
      }
    },
    Future.parallelObject(3, {
      whoami: KayttajaApi.whoami,
      roolit: KayttajaApi.roolit,
      aineistot: aineistotFuture,
      emptyAineistot: R.map(
        aineistot => KayttajaApi.deserializeKayttajaAineistot(aineistot)([]),
        aineistotFuture
      )
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
