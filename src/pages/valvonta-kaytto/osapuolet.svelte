<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';

  import * as Osapuolet from './osapuolet';
  import * as Links from './links';

  import { _, locale } from '@Language/i18n';
  import H2 from '@Component/H/H2.svelte';
  import Link from '@Component/Link/Link';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde.osapuolet';

  export let valvonta;
  export let henkilot;
  export let yritykset;
  export let roolit;
  export let toimitustavat;
  export let countries;

  const postiosoite = osapuoli =>
    osapuoli.jakeluosoite + ', ' +
    osapuoli.postinumero + ' ' +
    osapuoli.postitoimipaikka + ', ' +
    Locales.labelForId($locale, countries)(osapuoli['maa']);
</script>

<H2 text={i18n(i18nRoot + '.title')}/>
{#if R.isEmpty(henkilot) && R.isEmpty(yritykset)}
  {i18n(i18nRoot + '.empty')}
{:else}
  <div class="overflow-x-auto">
    <table class="etp-table">
      <thead class="etp-table--thead">
      <tr class="etp-table--tr etp-table--tr__light">
        <th class="etp-table--th">
          {i18n(i18nRoot + '.nimi')}
        </th>
        <th class="etp-table--th">
          {i18n(i18nRoot + '.rooli')}
        </th>
        <th class="etp-table--th">
          {i18n(i18nRoot + '.osoite')}
        </th>
        <th class="etp-table--th">
          {i18n(i18nRoot + '.email')}
        </th>
        <th class="etp-table--th">
          {i18n(i18nRoot + '.toimitustapa')}
        </th>
      </tr>
      </thead>
      <tbody class="etp-table--tbody">
      {#each henkilot as henkilo}
        <tr class="etp-table-tr">
          <td class="etp-table--td">
            <Link
                href={Links.henkilo(valvonta, henkilo)}
                text={`${henkilo.etunimi} ${henkilo.sukunimi}`}/>
          </td>
          <td class="etp-table--td">
            {Locales.labelForId($locale, roolit)(henkilo['rooli-id'])}
            {#if Osapuolet.otherRooli(henkilo)}
              {`- ${henkilo['rooli-description']}`}
            {/if}
          </td>
          <td class="etp-table--td">
            {postiosoite(henkilo)}
          </td>
          <td class="etp-table--td">
            <Link
                href={`mailto:${henkilo.email}`}
                text={henkilo.email}/>
          </td>
          <td class="etp-table--td">
            {Locales.labelForId(
              $locale,
              toimitustavat
            )(henkilo['toimitustapa-id'])}
            {#if Osapuolet.toimitustapa.other(henkilo)}
              {`- ${henkilo['toimitustapa-description']}`}
            {/if}
          </td>
        </tr>
      {/each}
      {#each yritykset as yritys}
        <tr class="etp-table-tr">
          <td class="etp-table--td">
            <Link
                href={Links.yritys(valvonta, yritys)}
                text={yritys.nimi}/>
          </td>
          <td class="etp-table--td">
            {Locales.labelForId($locale, roolit)(yritys['rooli-id'])}
            {#if Osapuolet.otherRooli(yritys)}
              {`- ${yritys['rooli-description']}`}
            {/if}
          </td>
          <td class="etp-table--td">
            {postiosoite(yritys)}
          </td>
          <td class="etp-table--td">
            <Link
                href={`mailto:${yritys.email}`}
                text={yritys.email}/>
          </td>
          <td class="etp-table--td">
            {Locales.labelForId(
              $locale,
              toimitustavat
            )(yritys['toimitustapa-id'])}
            {#if Osapuolet.toimitustapa.other(yritys)}
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
        href='{Links.newHenkilo(valvonta)}'
        icon={Maybe.Some('add_circle_outline')}
        text={i18n(i18nRoot + '.new-henkilo')}/>
  </div>
  <div class="flex mb-auto">
    <Link
        href='{Links.newYritys(valvonta)}'
        icon={Maybe.Some('add_circle_outline')}
        text={i18n(i18nRoot + '.new-yritys')}/>
  </div>
</div>