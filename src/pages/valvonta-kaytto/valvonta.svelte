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
  import * as geoApi from '@Component/Geo/geo-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Link from '@Component/Link/Link';

  import Manager from './manager.svelte';
  import Toimenpide from './toimenpide.svelte';
  import Note from './note.svelte';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.valvonta';

  export let params;
  export let resources = Maybe.None();
  let osapuolet = Maybe.None();
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
        osapuolet;
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
            whoami: Future.resolve(whoami),
            roolit: ValvontaApi.roolit,
            toimitustavat: ValvontaApi.toimitustavat,
            countries: geoApi.countries
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
    {#each Maybe.toArray(resources) as { toimenpiteet, notes, toimenpidetyypit, templatesByType, valvojat, henkilot, yritykset, valvonta, whoami, roolit, toimitustavat, countries }}
      <H1
        text={i18n(i18nRoot + '.title') +
          Maybe.fold('', R.concat(' - '), diaarinumero(toimenpiteet))} />

      <div class="flex flex-col my-4">
        <span>{`${valvonta.katuosoite}, ${valvonta.postinumero}`}</span>
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

      <H2 text={i18n(i18nRoot + '.osapuolet.title')} />

      {#if R.isEmpty(henkilot) && R.isEmpty(yritykset)}
        {i18n(i18nRoot + '.osapuolet.empty')}
      {:else}
        <div class="overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osapuolet.nimi')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osapuolet.rooli')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osapuolet.osoite')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osapuolet.email')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osapuolet.toimitustapa')}
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each henkilot as henkilo}
                <tr class="etp-table-tr">
                  <td class="etp-table--td">
                    <Link
                      href={`/#/valvonta/kaytto/${params.id}/henkilo/${henkilo.id}`}
                      text={`${henkilo.etunimi} ${henkilo.sukunimi}`} />
                  </td>
                  <td class="etp-table--td">
                    {Locales.labelForId($locale, roolit)(henkilo['rooli-id'])}
                    {#if henkilo['rooli-id'] === 2}
                      {`- ${henkilo['rooli-description']}`}
                    {/if}
                  </td>
                  <td class="etp-table--td">
                    {`${henkilo.jakeluosoite}, 
                    ${henkilo.postinumero} 
                    ${henkilo.postitoimipaikka},
                    ${Locales.labelForId($locale, countries)(henkilo['maa'])}`}
                  </td>
                  <td class="etp-table--td">
                    <Link
                      href={`mailto:${henkilo.email}`}
                      text={henkilo.email} />
                  </td>
                  <td class="etp-table--td">
                    {Locales.labelForId(
                      $locale,
                      toimitustavat
                    )(henkilo['toimitustapa-id'])}
                    {#if henkilo['toimitustapa-id'] === 2}
                      {`- ${henkilo['toimitustapa-description']}`}
                    {/if}
                  </td>
                </tr>
              {/each}
              {#each yritykset as yritys}
                <tr class="etp-table-tr">
                  <td class="etp-table--td">
                    <Link
                      href={`/#/valvonta/kaytto/${params.id}/yritys/${yritys.id}`}
                      text={yritys.nimi} />
                  </td>
                  <td class="etp-table--td">
                    {Locales.labelForId($locale, roolit)(yritys['rooli-id'])}
                    {#if yritys['rooli-id'] === 2}
                      {`- ${yritys['rooli-description']}`}
                    {/if}
                  </td>
                  <td class="etp-table--td">
                    {`${yritys.jakeluosoite}, 
                    ${yritys.postinumero} 
                    ${yritys.postitoimipaikka},
                    ${Locales.labelForId($locale, countries)(yritys['maa'])}`}
                  </td>
                  <td class="etp-table--td">
                    <Link href={`mailto:${yritys.email}`} text={yritys.email} />
                  </td>
                  <td class="etp-table--td">
                    {Locales.labelForId(
                      $locale,
                      toimitustavat
                    )(yritys['toimitustapa-id'])}
                    {#if yritys['toimitustapa-id'] === 2}
                      {`- ${yritys['toimitustapa-description']}`}
                    {/if}
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}

      <div class="flex my-4 space-x-4">
        <div class="flex mb-auto">
          <Link
            href="/#/valvonta/kaytto/{params.id}/henkilo/new"
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.osapuolet.new-henkilo')} />
        </div>
        <div class="flex mb-auto">
          <Link
            href="/#/valvonta/kaytto/{params.id}/yritys/new"
            icon={Maybe.Some('add_circle_outline')}
            text={i18n(i18nRoot + '.osapuolet.new-yritys')} />
        </div>
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
