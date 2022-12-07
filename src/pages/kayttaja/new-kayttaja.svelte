<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Response from '@Utility/response';

  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import { push } from '@Component/Router/router';

  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import KayttajaForm from './kayttaja-form.svelte';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import H1 from '@Component/H/H1.svelte';

  const i18n = $_;
  const i18nRoot = 'kayttaja.new';

  let resources = Maybe.None();
  const emptyKayttaja = {
    login: Maybe.None(),
    passivoitu: false,
    rooli: Maybe.None(),
    etunimi: '',
    sukunimi: '',
    email: '',
    puhelin: '',
    virtu: Maybe.None(),
    henkilotunnus: Maybe.None(),
    'api-key': Maybe.None()
  };

  let kayttaja = emptyKayttaja;
  let overlay = true;
  let dirty = false;

  const addKayttaja = kayttaja => {
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
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      roolit: KayttajaApi.roolit
    })
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    <H1 text={i18n(i18nRoot + '.title')} />
    {#each resources.toArray() as { whoami, roolit }}
      <KayttajaForm
        submit={addKayttaja}
        cancel={_ => {
          kayttaja = emptyKayttaja;
        }}
        bind:dirty
        {kayttaja}
        {whoami}
        {roolit} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
