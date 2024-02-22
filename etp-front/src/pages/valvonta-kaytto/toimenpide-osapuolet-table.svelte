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
  import Input from '@Component/Input/Input.svelte';

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
  export let schema;
  export let hallintoOikeudet = [];
  export let karajaoikeudet = [];
  export let manuallyDeliverableToimenpide = false;
  export let showDeliveryMethod = false;
  export let showHallintoOikeudetSelection = false;
  export let showKarajaOikeudetSelection = false;
  export let showCreateDocument = false;
  export let allowPreviewAlways = false;

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

  const osapuoliSpecificDataIndexForOsapuoli = osapuoli =>
    Toimenpiteet.osapuoliSpecificDataIndexForOsapuoli(
      toimenpide,
      osapuoli.id,
      Osapuolet.getOsapuoliType(osapuoli)
    );

  const isPreviewHidden = (toimenpide, osapuoli) => {
    if (allowPreviewAlways) {
      return false;
    } else {
      return !Toimenpiteet.documentExistsForOsapuoli(
        toimenpide,
        osapuoli.id,
        Osapuolet.getOsapuoliType(osapuoli)
      );
    }
  };
</script>

<style type="text/postcss">
  .court-container {
    height: 5rem;
  }

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

          {#if showDeliveryMethod}
            <th class="etp-table--th">
              {i18n(i18nRoot + '.toimitustapa')}
            </th>
          {/if}

          {#if showHallintoOikeudetSelection}
            <th class="etp-table--th">
              {i18n(i18nRoot + '.administrative-court')}
            </th>
          {/if}

          {#if showKarajaOikeudetSelection}
            <th class="etp-table--th">
              {i18n(i18nRoot + '.decision-order-notice-bailiff.district-court')}
            </th>

            <th class="etp-table--th">
              {i18n(i18nRoot + '.decision-order-notice-bailiff.bailiff-email')}
            </th>
          {/if}

          <th class="etp-table--th">
            {i18n(i18nRoot + '.esikatselu')}
          </th>

          {#if showCreateDocument}
            <th class="etp-table--th">{i18n(i18nRoot + '.create-document')}</th>
          {/if}
        </tr>
      </thead>
      <tbody class="etp-table--tbody">
        {#each osapuolet as osapuoli, i}
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

            {#if showDeliveryMethod}
              <td
                class="etp-table--td"
                class:text-error={Maybe.isSome(
                  osapuoli.type.errorKey(osapuoli)
                )}
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
            {/if}

            {#if showHallintoOikeudetSelection}
              <td class="etp-table--td court-container">
                {#if Osapuolet.isOmistaja(osapuoli) && Toimenpiteet.documentExistsForOsapuoli(toimenpide, osapuoli.id, Osapuolet.getOsapuoliType(osapuoli))}
                  <Select
                    bind:model={toimenpide}
                    name={'administrative-court-selector-' + i}
                    lens={R.lensPath([
                      'type-specific-data',
                      'osapuoli-specific-data',
                      osapuoliSpecificDataIndexForOsapuoli(osapuoli),
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
                        osapuoliSpecificDataIndexForOsapuoli(osapuoli),
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
            {/if}

            {#if showKarajaOikeudetSelection}
              <td class="etp-table--td court-container">
                {#if Osapuolet.isOmistaja(osapuoli) && Toimenpiteet.documentExistsForOsapuoli(toimenpide, osapuoli.id, Osapuolet.getOsapuoliType(osapuoli))}
                  <Select
                    bind:model={toimenpide}
                    name={'karajaoikeus-selector-' + i}
                    lens={R.lensPath([
                      'type-specific-data',
                      'osapuoli-specific-data',
                      osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                      'karajaoikeus-id'
                    ])}
                    modelToItem={Maybe.fold(
                      Maybe.None(),
                      Maybe.findById(R.__, karajaoikeudet)
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
                        osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                        'karajaoikeus-id'
                      ],
                      schema
                    )}
                    items={Selects.addNoSelection(
                      R.filter(isValid, karajaoikeudet)
                    )} />
                {:else}
                  {i18n('valvonta.kaytto.toimenpide.no-delivery')}
                {/if}
              </td>
              <td class="etp-table--td">
                {#if Osapuolet.isOmistaja(osapuoli) && Toimenpiteet.documentExistsForOsapuoli(toimenpide, osapuoli.id, Osapuolet.getOsapuoliType(osapuoli))}
                  <Input
                    bind:model={toimenpide}
                    name={'haastemies-email-' + i}
                    required={true}
                    type="email"
                    lens={R.lensPath([
                      'type-specific-data',
                      'osapuoli-specific-data',
                      osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                      'haastemies-email'
                    ])}
                    validators={R.path(
                      [
                        'type-specific-data',
                        'osapuoli-specific-data',
                        osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                        'haastemies-email'
                      ],
                      schema
                    )}
                    format={Maybe.orSome('')}
                    parse={R.ifElse(
                      R.isEmpty,
                      R.always(Maybe.None()),
                      Maybe.Some
                    )}
                    {i18n} />
                {:else}
                  {i18n('valvonta.kaytto.toimenpide.no-delivery')}
                {/if}
              </td>
            {/if}

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
                    class:invisible={isPreviewHidden(toimenpide, osapuoli)}
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

            {#if showCreateDocument}
              <td class="etp-table--td">
                <div class="etp-table--td__center">
                  {#if Osapuolet.isOmistaja(osapuoli)}
                    <Checkbox
                      bind:model={toimenpide}
                      lens={R.lensPath([
                        'type-specific-data',
                        'osapuoli-specific-data',
                        osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                        'document'
                      ])} />
                  {/if}
                </div>
              </td>
            {/if}
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</div>
