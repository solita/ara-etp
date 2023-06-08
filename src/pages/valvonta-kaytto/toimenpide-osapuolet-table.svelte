<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Osapuolet from './osapuolet';
  import * as Templates from './templates';
  import * as Toimenpiteet from './toimenpiteet';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';
  import H2 from '@Component/H/H2.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  export let id;
  export let toimenpide;
  export let henkilot;
  export let yritykset;
  export let preview;
  export let roolit;
  export let toimitustavat;
  export let template;
  export let previewPending;
  export let disabled;

  export let manuallyDeliverableToimenpide = false;

  const types = {
    yritys: {
      label: yritys => yritys.nimi,
      preview: ValvontaApi.previewToimenpideForYritysOsapuoli,
      errorKey: Osapuolet.toimitustapaErrorKey.yritys
    },
    henkilo: {
      label: henkilo => `${henkilo.etunimi} ${henkilo.sukunimi}`,
      preview: ValvontaApi.previewToimenpideForHenkiloOsapuoli,
      errorKey: Osapuolet.toimitustapaErrorKey.henkilo
    }
  };

  let osapuolet = R.sort(R.ascend(R.prop('toimitustapa-id')))(
    R.concat(
      R.map(R.assoc('type', types.henkilo), henkilot),
      R.map(R.assoc('type', types.yritys), yritykset)
    )
  );

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
              {osapuoli.type.label(osapuoli)}
            </td>
            <td class="etp-table--td">
              {rooliLabel(osapuoli)}
              {#if Osapuolet.otherRooli(osapuoli)}
                - {Maybe.orSome('', osapuoli['rooli-description'])}
              {/if}
            </td>
            <td
              class="etp-table--td"
              class:text-error={Maybe.isSome(osapuoli.type.errorKey(osapuoli))}
              title={Maybe.fold(
                '',
                key =>
                  i18n('valvonta.kaytto.osapuoli.toimitustapa-errors.' + key),
                osapuoli.type.errorKey(osapuoli)
              )}>
              {#if Maybe.isSome(osapuoli.type.errorKey(osapuoli))}
                <span class="font-icon">warning</span>
              {/if}
              {#if manuallyDeliverableToimenpide}
                {i18n('valvonta.kaytto.toimenpide.manually-sent')}
              {:else}
                {toimitustapaLabel(osapuoli)}
              {/if}
              {#if Osapuolet.toimitustapa.other(osapuoli)}
                - {Maybe.orSome('', osapuoli['toimitustapa-description'])}
              {/if}
            </td>
            <td class="etp-table--td">
              {#if Osapuolet.isOmistaja(osapuoli)}
                {#if previewPending}
                  <div class="etp-table--td__center">
                    <Spinner smaller={true} />
                  </div>
                {:else}
                  <div
                    class:text-primary={!disabled}
                    class:text-disabled={disabled}
                    class="cursor-pointer etp-table--td__center"
                    on:click|stopPropagation={disabled ||
                      preview(
                        osapuoli.type.preview(id, osapuoli.id, toimenpide)
                      )}>
                    <span class="font-icon text-2xl"> visibility </span>
                  </div>
                {/if}
              {:else if Maybe.orSome(false, R.lift(Templates.sendTiedoksi)(template))}
                <span class="font-icon">info</span>
                {i18n(i18nRoot + '.fyi')}
              {:else}
                <span class="font-icon">info</span>
                {i18n(i18nRoot + '.fyi-disabled')}
              {/if}
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</div>
