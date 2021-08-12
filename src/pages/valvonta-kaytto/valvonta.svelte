<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as ValvontaApi from './valvonta-api';
  import * as GeoApi from '@Component/Geo/geo-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Address from '@Component/address/building-address';

  import Manager from './manager.svelte';
  import Toimenpide from './toimenpide.svelte';
  import Note from './note.svelte';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.valvonta';

  export let params;
  export let resources = Maybe.None();
  let overlay = true;

  $: load(params);

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        const msg = Response.notFound(response)
          ? i18n(`${i18nRoot}.messages.not-found`)
          : i18n(
              Maybe.orSome(
                `${i18nRoot}.messages.load-error`,
                Response.localizationKey(response)
              )
            );
        flashMessageStore.add('valvonta-kaytto', 'error', msg);
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      R.chain(
        whoami =>
          Future.parallelObject(7, {
            toimenpiteet: ValvontaApi.toimenpiteet(params.id),
            notes: Future.resolve([]),
            toimenpidetyypit: ValvontaApi.toimenpidetyypit,
            templatesByType: ValvontaApi.templatesByType,
            valvojat: ValvontaApi.valvojat,
            henkilot: ValvontaApi.getHenkilot(params.id),
            yritykset: ValvontaApi.getYritykset(params.id),
            valvonta: ValvontaApi.valvonta(params.id),
            postinumerot: GeoApi.postinumerot,
            whoami: Future.resolve(whoami)
          }),
        KayttajaApi.whoami
      )
    );
  };

  const fork = (key, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.${key}-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-kaytto', 'error', msg);
        overlay = false;
      },
      response => {
        flashMessageStore.add(
          'valvonta-kaytto',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        overlay = false;
        successCallback(response);
      },
      future
    );
  };

  const saveValvonta = R.compose(
    fork('update', _ => load(params)),
    ValvontaApi.putValvonta(params.id)
  );

  const diaarinumero = R.compose(
    R.chain(R.prop('diaarinumero')),
    Maybe.fromNull,
    R.last
  );

  const tapahtumat = R.compose(
    R.reverse,
    R.sortBy(R.prop('create-time')),
    R.unnest
  );

  const isToimenpide = R.has('type-id');
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { toimenpiteet, notes, toimenpidetyypit, templatesByType, postinumerot, valvojat, henkilot, yritykset, valvonta, whoami }}
      <H1
        text={i18n(i18nRoot + '.title') +
          Maybe.fold('', R.concat(' - '), diaarinumero(toimenpiteet))} />

      <div class="flex flex-col my-4">
        <Address {postinumerot}
                 katuosoite={Maybe.Some(valvonta.katuosoite)}
                 postinumero={valvonta.postinumero} />
        <span>
          {`${i18n(i18nRoot + '.rakennustunnus')}: ${valvonta.rakennustunnus}`}
        </span>
        <span>
          {`${i18n(i18nRoot + '.ilmoitustunnus')}: ${valvonta.ilmoitustunnus}`}
        </span>
      </div>

      <Manager
        {valvojat}
        {valvonta}
        {henkilot}
        {yritykset}
        {toimenpiteet}
        {toimenpidetyypit}
        {templatesByType}
        {saveValvonta}
        reload={_ => load(params)} />

      <H2 text={i18n(i18nRoot + '.toimenpiteet')} />

      {#each tapahtumat([toimenpiteet, notes]) as tapahtuma}
        {#if isToimenpide(tapahtuma)}
          <Toimenpide
            {valvonta}
            {henkilot}
            {yritykset}
            {toimenpidetyypit}
            toimenpide={tapahtuma}
            {whoami} />
        {:else}
          <Note note={tapahtuma} />
        {/if}
      {/each}
      {#if R.isEmpty(tapahtumat)}
        <p>{i18n(i18nRoot + '.ei-tapahtumia')}</p>
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
