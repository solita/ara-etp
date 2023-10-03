<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Osapuolet from './osapuolet';
  import * as Templates from './templates';

  import * as ValvontaApi from './valvonta-api';

  import { _, locale } from '@Language/i18n';
  import H2 from '@Component/H/H2.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import * as Selects from '@Component/Select/select-util';
  import { isValid } from '@Utility/classification';
  import Select from '@Component/Select/Select2';
  import * as Toimenpiteet from '@Pages/valvonta-kaytto/toimenpiteet';
  import Checkbox from '@Component/Checkbox/Checkbox';

  export let id;
  export let toimenpide;
  export let henkilot;
  export let yritykset;
  export let preview;
  export let roolit;
  export let template;
  export let previewPending;
  export let disabled;
  export let schema;
  export let hallintoOikeudet = [];

  const types = {
    yritys: {
      label: yritys => yritys.nimi,
      preview: ValvontaApi.previewToimenpideForYritysOsapuoli
    },
    henkilo: {
      label: henkilo => `${henkilo.etunimi} ${henkilo.sukunimi}`,
      preview: ValvontaApi.previewToimenpideForHenkiloOsapuoli
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

  const courtDataIndexForOsapuoli = osapuoliId =>
    Toimenpiteet.courtDataIndexForOsapuoli(toimenpide, osapuoliId);
</script>

<style type="text/postcss">
  .hao-container {
    height: 5rem;
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
          <th class="etp-table--th"> {i18n(i18nRoot + '.court')} </th>

          <th class="etp-table--th">
            {i18n(i18nRoot + '.toimitustapa')}
          </th>

          <th class="etp-table--th">
            {i18n(i18nRoot + '.esikatselu')}
          </th>
          <th class="etp-table--th">{i18n(i18nRoot + '.create-document')}</th>
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
            <td class="etp-table--td hao-container">
              {#if Osapuolet.isOmistaja(osapuoli) && Toimenpiteet.documentExistsForOsapuoli(toimenpide, osapuoli.id)}
                <Select
                  bind:model={toimenpide}
                  lens={R.lensPath([
                    'type-specific-data',
                    'osapuoli-specific-data',
                    courtDataIndexForOsapuoli(osapuoli.id),
                    'hallinto-oikeus-id'
                  ])}
                  modelToItem={Maybe.fold(
                    Maybe.None(),
                    Maybe.findById(R.__, hallintoOikeudet)
                  )}
                  itemToModel={Maybe.fold(Maybe.None(), it =>
                    Maybe.Some(it.id)
                  )}
                  format={Maybe.fold(
                    i18n('validation.no-selection'),
                    Locales.label($locale)
                  )}
                  validators={R.path(
                    [
                      'type-specific-data',
                      'osapuoli-specific-data',
                      courtDataIndexForOsapuoli(osapuoli.id),
                      'hallinto-oikeus-id'
                    ],
                    schema
                  )}
                  items={Selects.addNoSelection(
                    R.filter(isValid, hallintoOikeudet)
                  )} />
              {:else}
                {i18n('valvonta.kaytto.toimenpide.no-delivery')}
              {/if}
            </td>
            <td class="etp-table--td">
              {i18n('valvonta.kaytto.toimenpide.manually-sent')}
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
                    role="button"
                    hidden={!Toimenpiteet.documentExistsForOsapuoli(
                      toimenpide,
                      osapuoli.id
                    )}
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
            <td class="etp-table--td__center">
              {#if Osapuolet.isOmistaja(osapuoli)}
                <Checkbox
                  bind:model={toimenpide}
                  lens={R.lensPath([
                    'type-specific-data',
                    'osapuoli-specific-data',
                    courtDataIndexForOsapuoli(osapuoli.id),
                    'document'
                  ])} />
              {/if}
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</div>
