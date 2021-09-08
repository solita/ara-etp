<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import { push } from '@Component/Router/router';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import KohdeForm from './kohde-form';
  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as api from './valvonta-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';
  const emptyKohde = _ => ({
    rakennustunnus: Maybe.None(),
    katuosoite: '',
    postinumero: Maybe.None(),
    'ilmoituspaikka-id': Maybe.None(),
    'ilmoituspaikka-description': Maybe.None(),
    ilmoitustunnus: Maybe.None(),
    havaintopaiva: Either.Right(Maybe.None()),
    'valvoja-id': Maybe.None()
  });

  let overlay = true;
  let dirty = false;
  let kohde = emptyKohde();

  let resources = Maybe.None();

  const resetForm = _ => {
    kohde = emptyKohde();
  };

  Future.fork(
    response => {
      const msg = $_(
        `${i18nRoot}.messages.load-error`,
        Response.localizationKey(response)
      );

      flashMessageStore.add('kohde', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
      dirty = false;
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      ilmoituspaikat: api.ilmoituspaikat
    })
  );

  const addKohde = kohde => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-kaytto', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'valvonta-kaytto',
          'success',
          i18n(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + _.id + '/kohde');
      },
      api.postValvonta(fetch, kohde));
  };
</script>

<H1 text={i18n(i18nRoot + '.new-kohde')} />

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { ilmoituspaikat }}
      <DirtyConfirmation {dirty} />
      <KohdeForm bind:dirty {kohde} {ilmoituspaikat}
                 save={addKohde}
                 revert={resetForm} />
      <div class="flex">
        <span>{i18n(`${i18nRoot}.after-save-notice`)}</span>
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
