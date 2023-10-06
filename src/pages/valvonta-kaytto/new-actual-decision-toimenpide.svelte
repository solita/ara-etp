<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Parsers from '@Utility/parsers';
  import Select2 from '@Component/Select/Select2';
  import Textarea from '@Component/Textarea/Textarea';
  import Input from '@Component/Input/Input';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';
  import * as Osapuolet from '@Pages/valvonta-kaytto/osapuolet';
  import H2 from '@Component/H/H2';
  import * as Toimenpiteet from '@Pages/valvonta-kaytto/toimenpiteet';

  export let toimenpide;
  export let henkilot;
  export let yritykset;
  export let text;
  export let i18n;
  export let schema;

  // TODO: Siisti tämä, previewiä ei tarvita tässä
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

  const courtDataIndexForOsapuoli = osapuoliId =>
    Toimenpiteet.courtDataIndexForOsapuoli(toimenpide, osapuoliId);
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

  {#each osapuolet as osapuoli}
    <H2 text={osapuoli.type.label(osapuoli)} />

    <div class="w-full py-4">
      <Select2
        bind:model={toimenpide}
        lens={R.lensPath([
          'type-specific-data',
          'osapuoli-specific-data',
          courtDataIndexForOsapuoli(osapuoli.id),
          'recipient-answered'
        ])}
        format={value => text(toimenpide, `hearing-letter-answered.${value}`)}
        label={text(toimenpide, 'hearing-letter-answered.label')}
        required
        items={[true, false]} />
    </div>

    <div class="w-full py-4">
      <Textarea
        id={'toimenpide.answer-commentary-fi'}
        name={'toimenpide.answer-commentary-fi'}
        label={text(toimenpide, 'answer-commentary-fi')}
        bind:model={toimenpide}
        lens={R.lensPath([
          'type-specific-data',
          'osapuoli-specific-data',
          courtDataIndexForOsapuoli(osapuoli.id),
          'answer-commentary-fi'
        ])}
        required
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={R.path(
          [
            'type-specific-data',
            'osapuoli-specific-data',
            courtDataIndexForOsapuoli(osapuoli.id),
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
          courtDataIndexForOsapuoli(osapuoli.id),
          'answer-commentary-sv'
        ])}
        required
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={R.path(
          [
            'type-specific-data',
            'osapuoli-specific-data',
            courtDataIndexForOsapuoli(osapuoli.id),
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
          courtDataIndexForOsapuoli(osapuoli.id),
          'statement-fi'
        ])}
        required
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={R.path(
          [
            'type-specific-data',
            'osapuoli-specific-data',
            courtDataIndexForOsapuoli(osapuoli.id),
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
          courtDataIndexForOsapuoli(osapuoli.id),
          'statement-sv'
        ])}
        required
        format={Maybe.orSome('')}
        parse={Parsers.optionalString}
        validators={R.path(
          [
            'type-specific-data',
            'osapuoli-specific-data',
            courtDataIndexForOsapuoli(osapuoli.id),
            'statement-sv'
          ],
          schema
        )}
        {i18n} />
    </div>
  {/each}
</div>
