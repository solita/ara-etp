<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
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

<div>
  <table class="etp-table">
    <thead class="etp-table--thead">
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkausaika')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkaaja')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.patevyystaso')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.www-osoite')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.osoite')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.julkiset')}</th>
      <th class="etp-table--th">{i18n('laatija.paatoimintaalue')}</th>
      <th class="etp-table--th">{i18n('laatija.muuttoimintaalueet')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.toteaja')}</th>
      <th class="etp-table--th">{i18n('laatija.upload.toteamispaivamaara')}</th>
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
            {Locales.labelForId($locale, patevyydet)(h.patevyystaso)}

            {#if h.laatimiskielto}
              <span class="text-error font-bold p-1">
                {i18n(i18nRoot + '.laatimiskielto')}
              </span>
            {/if}
          </td>
          <td class="etp-table--td">
            {h.wwwosoite.orSome('-')}
          </td>
          <td class="etp-table--td">
            {h.jakeluosoite},
            {h.postinumero}
            {h.postitoimipaikka},
            {h.maa}
          </td>
          <td class="etp-table--td">
            {i18n(i18nRoot + '.osoite') +
              `: ${h.julkinenosoite ? i18n('yes') : i18n('no')}`}
            {i18n(i18nRoot + '.puhelin') +
              `: ${h.julkinenpuhelin ? i18n('yes') : i18n('no')}`}
            {i18n(i18nRoot + '.email') +
              `: ${h.julkinenemail ? i18n('yes') : i18n('no')}`}
            {i18n(i18nRoot + '.www-osoite') +
              `: ${h.julkinenwwwosoite ? i18n('yes') : i18n('no')}`}
          </td>
          <td class="etp-table--td">
            <!-- {Locales.labelForId($locale, toimintaalueet)(h.toimintaalue)} -->
            {Maybe.fold(
              '-',
              Locales.labelForId($locale, toimintaalueet),
              h.toimintaalue
            )}
          </td>
          <td class="etp-table--td">
            {#each h.muuttoimintaalueet as alue}
              <span class="flex flex-no-wrap truncate">
                {Locales.labelForId($locale, toimintaalueet)(alue)}
              </span>
            {/each}
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
