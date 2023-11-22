<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Parsers from '@Utility/parsers';
  import Select2 from '@Component/Select/Select2';
  import Textarea from '@Component/Textarea/Textarea';
  import Input from '@Component/Input/Input';
  import * as Osapuolet from '@Pages/valvonta-kaytto/osapuolet';
  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';
  import * as Toimenpiteet from '@Pages/valvonta-kaytto/toimenpiteet';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';
  import * as Locales from '@Language/locale-utils';
  import { locale } from '@Language/i18n';
  import * as Selects from '@Component/Select/select-util';
  import { isValid } from '@Utility/classification';
  import TextButton from '@Component/Button/TextButton';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';
  import Spinner from '@Component/Spinner/Spinner';
  import Error from '@Component/Error/Error';

  export let id;
  export let toimenpide;
  export let henkilot;
  export let yritykset;
  export let hallintoOikeudet = [];

  export let preview;
  export let previewPending;
  export let disabled;
  export let text;
  export let error;
  export let i18n;
  export let schema;

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
    R.filter(
      Osapuolet.isOmistaja,
      R.concat(
        R.map(R.assoc('type', types.henkilo), henkilot),
        R.map(R.assoc('type', types.yritys), yritykset)
      )
    )
  );

  const osapuoliSpecificDataIndexForOsapuoli = osapuoli =>
    Toimenpiteet.osapuoliSpecificDataIndexForOsapuoli(
      toimenpide,
      osapuoli.id,
      Osapuolet.getOsapuoliType(osapuoli)
    );

  const previewOsapuoliDocument = osapuoli => _ => {
    // Include osapuoli-specific-data for only the current osapuoli in the
    // preview api call and validation so unfilled osapuoli data doesn't
    // affect previewing filled ones
    const previewToimenpide = Toimenpiteet.toimenpideForOsapuoli(
      toimenpide,
      osapuoli.id,
      Osapuolet.getOsapuoliType(osapuoli)
    );
    preview(
      osapuoli.type.preview(id, osapuoli.id, previewToimenpide),
      previewToimenpide
    );
  };
</script>

<div>
  <div class="w-full py-4">
    <Input
      id="toimenpide.department-head-title-fi"
      name="toimenpide.department-head-title-fi"
      label={text(toimenpide, 'department-head-title-fi')}
      bind:model={toimenpide}
      lens={R.lensPath(['type-specific-data', 'department-head-title-fi'])}
      required={true}
      type="text"
      format={Maybe.orSome('')}
      parse={Parsers.optionalString}
      validators={R.path(
        ['type-specific-data', 'department-head-title-fi'],
        schema
      )}
      {i18n} />
  </div>

  <div class="w-full py-4">
    <Input
      id="toimenpide.department-head-title-sv"
      name="toimenpide.department-head-title-sv"
      label={text(toimenpide, 'department-head-title-sv')}
      bind:model={toimenpide}
      lens={R.lensPath(['type-specific-data', 'department-head-title-sv'])}
      required={true}
      type="text"
      format={Maybe.orSome('')}
      parse={Parsers.optionalString}
      validators={R.path(
        ['type-specific-data', 'department-head-title-sv'],
        schema
      )}
      {i18n} />
  </div>

  <div class="w-full py-4">
    <Input
      id="toimenpide.department-head-name"
      name="toimenpide.department-head-name"
      label={text(toimenpide, 'department-head-name')}
      bind:model={toimenpide}
      lens={R.lensPath(['type-specific-data', 'department-head-name'])}
      required={true}
      type="text"
      format={Maybe.orSome('')}
      parse={Parsers.optionalString}
      validators={R.path(
        ['type-specific-data', 'department-head-name'],
        schema
      )}
      {i18n} />
  </div>

  <div class="w-full py-4">
    <H2 text={text(toimenpide, 'osapuoli-specific-data')} />
    {#each osapuolet as osapuoli}
      <div class="py-4">
        <H3 text={osapuoli.type.label(osapuoli)} />
        <Checkbox
          bind:model={toimenpide}
          label={text(toimenpide, 'document')}
          lens={R.lensPath([
            'type-specific-data',
            'osapuoli-specific-data',
            osapuoliSpecificDataIndexForOsapuoli(osapuoli),
            'document'
          ])} />
        {#if Toimenpiteet.documentExistsForOsapuoli(toimenpide, osapuoli.id, Osapuolet.getOsapuoliType(osapuoli))}
          <div class="w-full py-4">
            <Select2
              bind:model={toimenpide}
              lens={R.lensPath([
                'type-specific-data',
                'osapuoli-specific-data',
                osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                'recipient-answered'
              ])}
              format={value =>
                text(toimenpide, `hearing-letter-answered.${value}`)}
              label={text(toimenpide, 'hearing-letter-answered.label')}
              required
              items={[true, false]} />
          </div>

          {#if Toimenpiteet.didRecipientAnswer(toimenpide, osapuoli)}
            <div class="w-full py-4">
              <Textarea
                id={'toimenpide.answer-commentary-fi'}
                name={'toimenpide.answer-commentary-fi'}
                label={text(toimenpide, 'answer-commentary-fi')}
                bind:model={toimenpide}
                lens={R.lensPath([
                  'type-specific-data',
                  'osapuoli-specific-data',
                  osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                  'answer-commentary-fi'
                ])}
                required
                format={Maybe.orSome('')}
                parse={Parsers.optionalString}
                validators={R.path(
                  [
                    'type-specific-data',
                    'osapuoli-specific-data',
                    osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                    'answer-commentary-fi'
                  ],
                  schema
                )}
                {i18n} />
            </div>
            <div class="w-full py-4">
              <Textarea
                id={'toimenpide.answer-commentary-sv'}
                name={'toimenpide.answer-commentary-sv'}
                label={text(toimenpide, 'answer-commentary-sv')}
                bind:model={toimenpide}
                lens={R.lensPath([
                  'type-specific-data',
                  'osapuoli-specific-data',
                  osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                  'answer-commentary-sv'
                ])}
                required
                format={Maybe.orSome('')}
                parse={Parsers.optionalString}
                validators={R.path(
                  [
                    'type-specific-data',
                    'osapuoli-specific-data',
                    osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                    'answer-commentary-sv'
                  ],
                  schema
                )}
                {i18n} />
            </div>

            <div class="w-full py-4">
              <Textarea
                id={'toimenpide.statement-fi'}
                name={'toimenpide.statement-fi'}
                label={text(toimenpide, 'statement-fi')}
                bind:model={toimenpide}
                lens={R.lensPath([
                  'type-specific-data',
                  'osapuoli-specific-data',
                  osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                  'statement-fi'
                ])}
                required
                format={Maybe.orSome('')}
                parse={Parsers.optionalString}
                validators={R.path(
                  [
                    'type-specific-data',
                    'osapuoli-specific-data',
                    osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                    'statement-fi'
                  ],
                  schema
                )}
                {i18n} />
            </div>

            <div class="w-full py-4">
              <Textarea
                id={'toimenpide.statement-sv'}
                name={'toimenpide.statement-sv'}
                label={text(toimenpide, 'statement-sv')}
                bind:model={toimenpide}
                lens={R.lensPath([
                  'type-specific-data',
                  'osapuoli-specific-data',
                  osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                  'statement-sv'
                ])}
                required
                format={Maybe.orSome('')}
                parse={Parsers.optionalString}
                validators={R.path(
                  [
                    'type-specific-data',
                    'osapuoli-specific-data',
                    osapuoliSpecificDataIndexForOsapuoli(osapuoli),
                    'statement-sv'
                  ],
                  schema
                )}
                {i18n} />
            </div>
          {/if}
          <div class="w-full py-4">
            <Select2
              bind:model={toimenpide}
              label={text(toimenpide, 'court')}
              required
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
              itemToModel={Maybe.fold(Maybe.None(), it => Maybe.Some(it.id))}
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
          </div>
          <div class="w-full py-6">
            <TextButton
              text={text(toimenpide, 'preview-button')}
              icon="visibility"
              type="button"
              on:click={previewOsapuoliDocument(osapuoli)}
              {disabled} />
            {#if previewPending}
              <Spinner smaller={true} />
            {/if}
            {#each error.toArray() as txt}
              <Error text={txt} />
            {/each}
          </div>
        {/if}
      </div>
    {/each}
  </div>

  <div class="w-full pt-4 border-t-1">
    <p>
      <span class="font-icon mr-1">info</span>{i18n(
        'valvonta.kaytto.toimenpide.manually-sent'
      )}
    </p>
  </div>
</div>
