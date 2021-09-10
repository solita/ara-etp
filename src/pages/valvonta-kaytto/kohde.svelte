<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import { push } from '@Component/Router/router';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as ValvontaApi from './valvonta-api';
  import * as geoApi from '@Component/Geo/geo-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Liitteet from '@Component/liitteet/liitteet.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import KohdeForm from './kohde-form';
  import Osapuolet from './osapuolet.svelte';

  export let params;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';

  let overlay = true;
  let dirtyKohde = false;
  let dirtyLiitteet = false;
  let form;

  let resources = Maybe.None();

  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add('valvonta-kaytto', 'error',
          i18n(Response.errorKey(i18nRoot, 'load', response)));
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirtyKohde = false;
        dirtyLiitteet = false;
      },
      Future.parallelObject(9, {
        valvonta: ValvontaApi.valvonta(id),
        liitteet: ValvontaApi.liitteet(id),
        whoami: KayttajaApi.whoami,
        ilmoituspaikat: ValvontaApi.ilmoituspaikat,
        roolit: ValvontaApi.roolit,
        henkilot: ValvontaApi.getHenkilot(params.id),
        yritykset: ValvontaApi.getYritykset(params.id),
        countries: geoApi.countries,
        postinumerot: geoApi.postinumerot,
        toimitustavat: ValvontaApi.toimitustavat
      })
    );
  };

  const fork = (action, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        flashMessageStore.add('valvonta-kaytto', 'error',
          i18n(Response.errorKey(i18nRoot, action, response)));
        overlay = false;
      },
      _ => {
        flashMessageStore.add('valvonta-kaytto', 'success',
          i18n(`${i18nRoot}.messages.${action}-success`));
        overlay = false;
        successCallback();
      },
      future
    );
  };

  const updateKohde = R.compose(
    fork('save', _ => load(params.id)),
    ValvontaApi.putValvonta(params.id)
  );

  const deleteKohde = R.compose(
    fork('delete', _ => push('/valvonta/kaytto/all')),
    ValvontaApi.deleteValvonta
  );

  const liiteOperation = (key, liiteFuture) => liite =>
    fork(key, _ => load(params.id))(liiteFuture(liite));

  const liiteApi = {
    getUrl: ValvontaApi.url.liitteet(params.id),

    addFiles: liiteOperation(
      'add-files',
      ValvontaApi.postLiitteetFiles(params.id)
    ),

    addLink: liiteOperation('add-link', ValvontaApi.postLiitteetLink(params.id)),

    deleteLiite: liiteOperation(
      'delete-liite',
      ValvontaApi.deleteLiite(params.id)
    )
  };

  $: load(params.id);
</script>

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as {
      whoami,
      valvonta,
      liitteet,
      ilmoituspaikat,
      roolit,
      toimitustavat,
      postinumerot,
      countries,
      henkilot,
      yritykset,
    }}

      <DirtyConfirmation dirty={dirtyKohde || dirtyLiitteet}/>
      <KohdeForm bind:dirty={dirtyKohde} kohde={valvonta} {ilmoituspaikat}
                 {postinumerot}
                 save={updateKohde}
                 revert={_ => load(params.id)}
                 remove={Maybe.Some(deleteKohde)}/>

      <Osapuolet {valvonta} {henkilot} {yritykset}
                 {roolit} {toimitustavat} {countries}/>

      <div class="flex flex-col">
        <H2 text={i18n(`${i18nRoot}.attachments`)}/>
        <Liitteet
            {liiteApi}
            bind:dirty={dirtyLiitteet}
            {liitteet}
            emptyMessageKey={`${i18nRoot}.no-attachments`}
            flashModule="valvonta-kaytto"/>
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner/>
  </div>
</Overlay>
