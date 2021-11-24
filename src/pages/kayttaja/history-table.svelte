<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';
  import { locale, _ } from '@Language/i18n';

  const i18n = $_;
  const i18nRoot = 'muutoshistoria';

  export let history = [];
  export let roolit = [];
</script>

<style>
</style>

<div class="overflow-x-auto">
  <table class="etp-table">
    <thead class="etp-table--thead">
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkausaika')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.muokkaaja')}</th>
      <th class="etp-table--th">{i18n('kayttajat.nimi')}</th>
      <th class="etp-table--th">{i18n('laatija.henkilotunnus')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.puhelin')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.email')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.rooli')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.virtu')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.cognitoid')}</th>
      <th class="etp-table--th">{i18n(i18nRoot + '.passivoitu')}</th>
    </thead>
    <tbody class="etp-table--tbody">
      {#each history as h}
        <tr class="etp-table--tr etp-table--tr">
          <td class="etp-table--td">
            {Formats.formatTimeInstant(h.modifytime)}
          </td>
          <td class="etp-table--td">
            {h['modifiedby-name']}
          </td>
          <td class="etp-table--td">
            {h.etunimi}
            {h.sukunimi}
          </td>
          <td class="etp-table--td">
            {h.henkilotunnus.orSome('-')}
          </td>
          <td class="etp-table--td">
            {h.puhelin}
          </td>
          <td class="etp-table--td">
            {h.email}
          </td>
          <td class="etp-table--td">
            {Locales.labelForId($locale, roolit)(h.rooli)}
          </td>
          <td class="etp-table--td">
            {h.virtu.orSome()?.localid || '-'}

            {#if h.virtu.orSome()?.organisaatio}
              <span class="font-icon text-primary">alternate_email</span>
              <span>
                {h.virtu.orSome().organisaatio}
              </span>
            {/if}
          </td>
          <td class="etp-table--td">
            {h.cognitoid.orSome('-')}
          </td>
          <td class="etp-table--td">
            {h.passivoitu ? i18n('yes') : i18n('no')}
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
