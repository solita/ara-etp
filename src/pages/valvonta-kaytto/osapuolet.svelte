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

  const types = {
    yritys: {
      label: yritys => yritys.nimi,
      link: (valvonta, yritys) => Links.yritys(valvonta, yritys)
    },
    henkilo: {
      label: henkilo => `${henkilo.etunimi} ${henkilo.sukunimi}`,
      link: (valvonta, henkilo) => Links.henkilo(valvonta, henkilo)
    }
  };

  let osapuolet = R.sort(R.ascend(R.prop('toimitustapa-id')))(
    R.concat(
      R.map(R.assoc('type', types.henkilo), henkilot),
      R.map(R.assoc('type', types.yritys), yritykset)
    )
  );

  const postinumeroLine = R.compose(
    Maybe.fromEmpty,
    R.join(' '),
    Maybe.findAllSome,
    R.juxt([R.prop('postinumero'), R.prop('postitoimipaikka')])
  );

  $: countryLabel = R.compose(
    R.map(Locales.labelForId($locale, countries)),
    R.prop('maa')
  );

  $: postiosoite = R.compose(
    R.join(', '),
    Maybe.findAllSome,
    R.juxt([R.prop('jakeluosoite'), postinumeroLine, countryLabel])
  );

  $: rooliLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, roolit)),
    R.prop('rooli-id')
  );

  $: toimitustapaLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, toimitustavat)),
    R.prop('toimitustapa-id')
  );
</script>

<H2 text={i18n(i18nRoot + '.title')} />
{#if R.isEmpty(osapuolet)}
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
        {#each osapuolet as osapuoli}
          <tr class="etp-table-tr">
            <td class="etp-table--td">
              <Link
                href={osapuoli.type.link(valvonta, osapuoli)}
                text={osapuoli.type.label(osapuoli)} />
            </td>
            <td class="etp-table--td">
              {rooliLabel(osapuoli)}
              {#if Osapuolet.otherRooli(osapuoli)}
                - {Maybe.orSome('', osapuoli['rooli-description'])}
              {/if}
            </td>
            <td class="etp-table--td">
              {postiosoite(osapuoli)}
            </td>
            <td class="etp-table--td">
              {#each Maybe.toArray(osapuoli.email) as email}
                <Link href={`mailto:${email}`} text={email} />
              {/each}
            </td>
            <td class="etp-table--td">
              {toimitustapaLabel(osapuoli)}
              {#if Osapuolet.toimitustapa.other(osapuoli)}
                - {Maybe.orSome('', osapuoli['toimitustapa-description'])}
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
      href={Links.newHenkilo(valvonta)}
      icon={Maybe.Some('add_circle_outline')}
      text={i18n(i18nRoot + '.new-henkilo')} />
  </div>
  <div class="flex mb-auto">
    <Link
      href={Links.newYritys(valvonta)}
      icon={Maybe.Some('add_circle_outline')}
      text={i18n(i18nRoot + '.new-yritys')} />
  </div>
</div>
