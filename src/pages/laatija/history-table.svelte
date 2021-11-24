<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';
  import { locale, _ } from '@Language/i18n';
  import Link from '@Component/Link/Link';

  const i18n = $_;
  const i18nRoot = 'muutoshistoria';

  export let history = [];
  export let patevyydet;
  export let toimintaalueet;
  export let countries;
  export let laskutuskielet;
</script>

<style>
</style>

<div class="overflow-x-auto">
  <table class="etp-table">
    <thead class="etp-table--thead">
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkausaika')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkaaja')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.patevyystaso')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.www-osoite')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.osoite')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.julkiset')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.toimintaalueet')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.toteaja')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.toteamispaivamaara')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.laskutuskieli')}</th>
    </thead><tbody class="etp-table--tbody">
      {#each history as h}
        <tr class="etp-table--tr etp-table--tr">
          <td class="etp-table--td">
            {Formats.formatTimeInstant(h.modifytime)}
          </td>
          <td class="etp-table--td">
            {h['modifiedby-name']}
          </td>
          <td class="etp-table--td">
            <span>
              {Locales.labelForId($locale, patevyydet)(h.patevyystaso)}
            </span>

            {#if h.laatimiskielto}
              <span class="text-error font-bold">
                {i18n(i18nRoot + '.laatimiskielto')}
              </span>
            {/if}
          </td>
          <td class="etp-table--td">
            {#if Maybe.isSome(h.wwwosoite)}
              <Link
                href={h.wwwosoite.orSome('')}
                text={h.wwwosoite.orSome('')} />
            {:else}
              {'-'}
            {/if}
          </td>
          <td class="etp-table--td">
            {h.jakeluosoite},
            {h.postinumero}
            {h.postitoimipaikka},
            {Maybe.fold(
              '',
              Locales.labelForId($locale, countries),
              Either.toMaybe(h.maa)
            )}
          </td>
          <td class="etp-table--td">
            <div class="flex flex-col">
              <span
                class={h.julkinenosoite
                  ? 'font-bold text-primary'
                  : 'text-disabled'}>
                {i18n(i18nRoot + '.osoite')}
              </span>
              <span
                class={h.julkinenpuhelin
                  ? 'font-bold text-primary'
                  : 'text-disabled'}>
                {i18n(i18nRoot + '.puhelin')}
              </span>
              <span
                class={h.julkinenemail
                  ? 'font-bold text-primary'
                  : 'text-disabled'}>
                {i18n(i18nRoot + '.email')}
              </span>
              <span
                class={h.julkinenwwwosoite
                  ? 'font-bold text-primary'
                  : 'text-disabled'}>
                {i18n(i18nRoot + '.www-osoite')}
              </span>
            </div>
          </td>
          <td class="etp-table--td">
            <div class="flex flex-col">
              <span
                class="font-bold text-primary"
                title={i18n('laatija.paatoimintaalue')}>
                {Maybe.fold(
                  '-',
                  Locales.labelForId($locale, toimintaalueet),
                  h.toimintaalue
                )}
              </span>
              {#each h.muuttoimintaalueet as alue}
                <span
                  class="flex flex-no-wrap truncate"
                  title={i18n('laatija.muuttoimintaalueet')}>
                  {Locales.labelForId($locale, toimintaalueet)(alue)}
                </span>
              {/each}
            </div>
          </td>
          <td class="etp-table--td">
            {h.toteaja}
          </td>
          <td class="etp-table--td">
            {Formats.formatDateInstant(h.toteamispaivamaara)}
          </td>
          <td class="etp-table--td">
            {Locales.labelForId($locale, laskutuskielet)(h.laskutuskieli)}
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
