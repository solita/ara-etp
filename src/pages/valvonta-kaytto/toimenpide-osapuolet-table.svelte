<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Locales from '@Language/locale-utils';

  import * as Osapuolet from './osapuolet';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';
  import H2 from '@Component/H/H2.svelte';

  export let id;
  export let toimenpide;
  export let henkilot;
  export let yritykset;
  export let preview;
  export let roolit;
  export let toimitustavat;

  let osapuolet = R.sort(R.ascend(R.prop('toimitustapa-id')))(
    R.concat(henkilot, yritykset)
  );

  $: hasErrorToimitustapaHtunnus = false;
  $: hasErrorToimitustapaYtunnus = false;
  $: hasErrorToimitustapaEmail = false;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.toimenpide';

  $: rooliLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, roolit)),
    R.prop('rooli-id')
  );
  $: toimitustapaLabel = R.compose(
    Maybe.fold('', Locales.labelForId($locale, toimitustavat)),
    R.prop('toimitustapa-id')
  );
</script>

<style>
  .etp-table--td.text-error {
    @apply font-bold;
  }
</style>

<div class="w-full">
  <H2 text={i18n(i18nRoot + '.vastaanottajat')} />
  <div class="flex flex-row my-4">
    <table class="etp-table">
      <table class="etp-table">
        <thead class="etp-table--thead">
          <tr class="etp-table--tr etp-table--tr__light">
            <th class="etp-table--th"> {i18n(i18nRoot + '.nimi')} </th>

            <th class="etp-table--th"> {i18n(i18nRoot + '.rooli')} </th>

            <th class="etp-table--th">
              {i18n(i18nRoot + '.toimitustapa')}
            </th>

            <th class="etp-table--th">
              {i18n(i18nRoot + '.esikatselu')}
            </th>
          </tr>
        </thead>
        <tbody class="etp-table--tbody">
          {#each osapuolet as osapuoli}
            <tr class="etp-table-tr">
              <td class="etp-table--td">
                {#if osapuoli.nimi}
                  {`${osapuoli.nimi}`}
                {:else}
                  {`${osapuoli.etunimi} ${osapuoli.sukunimi}`}
                {/if}
              </td>
              <td class="etp-table--td">
                {rooliLabel(osapuoli)}
                {#if Osapuolet.otherRooli(osapuoli)}
                  {`- ${osapuoli['rooli-description']}`}
                {/if}
              </td>
              <td
                class="etp-table--td"
                class:text-error={(Osapuolet.toimitustapa.email(osapuoli) &&
                  Maybe.isNone(osapuoli.email)) ||
                  (Osapuolet.toimitustapa.suomifi(osapuoli) &&
                    ((R.has('henkilotunnus')(osapuoli) &&
                      Maybe.isNone(osapuoli.henkilotunnus)) ||
                      (R.has('ytunnus')(osapuoli) &&
                        Maybe.isNone(osapuoli.ytunnus))))}>
                {toimitustapaLabel(osapuoli)}
                {#if Osapuolet.toimitustapa.other(osapuoli)}
                  {`- ${osapuoli['toimitustapa-description']}`}
                {/if}
              </td>
              <td class="etp-table--td">
                {#if osapuoli.nimi}
                  <div
                    class="text-primary cursor-pointer etp-table--td__center"
                    on:click|stopPropagation={preview(
                      ValvontaApi.previewToimenpideForYritysOsapuoli(
                        id,
                        osapuoli.id,
                        toimenpide
                      )
                    )}>
                    <span class="font-icon text-2xl"> visibility </span>
                  </div>
                {:else}
                  <div
                    class="text-primary cursor-pointer etp-table--td__center"
                    on:click|stopPropagation={preview(
                      ValvontaApi.previewToimenpideForHenkiloOsapuoli(
                        id,
                        osapuoli.id,
                        toimenpide
                      )
                    )}>
                    <span class="font-icon text-2xl"> visibility </span>
                  </div>
                {/if}
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    </table>
  </div>
</div>
<div class="w-full">
  {#if hasErrorToimitustapaHtunnus}
    <div class="w-full flex items-center space-x-2 ml-2">
      <span class="font-icon text-error text-2xl">info</span>
      <span>{i18n(i18nRoot + '.messages.error-toimitustapa-htunnus')}</span>
    </div>
  {:else if hasErrorToimitustapaYtunnus}
    <div class="w-full flex items-center space-x-2 ml-2">
      <span class="font-icon text-error text-2xl">info</span>
      <span>{i18n(i18nRoot + '.messages.error-toimitustapa-ytunnus')}</span>
    </div>
  {:else if hasErrorToimitustapaEmail}
    <div class="w-full flex items-center space-x-2 ml-2">
      <span class="font-icon text-error text-2xl">info</span>
      <span>{i18n(i18nRoot + '.messages.error-toimitustapa-email')}</span>
    </div>
  {/if}
</div>
