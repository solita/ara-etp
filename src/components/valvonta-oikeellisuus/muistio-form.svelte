<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import Datepicker from '@Component/Input/Datepicker.svelte';
  import Select from '@Component/Select/Select.svelte';
  import H2 from '@Component/H/H2.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide.audit-report';

  export let toimenpide;
  export let templates;
  export let severities;
  export let virhetyypit;
  export let disabled;
  export let schema;

  let virhetyyppi = Maybe.None();

  $: formatTemplate = Locales.labelForId($locale, templates);
  $: formatVirhetyyppi = Locales.labelForId($locale, virhetyypit);
  $: formatSeverity = Locales.labelForId($locale, severities);

  const addVirhe = virhetyyppiId => {
    const newVirhe = {
      'type-id': virhetyyppiId,
      'description': R.compose(
        Locales.prop('description', $locale),
        R.find(R.propEq('id', virhetyyppiId)))(virhetyypit)
    };

    toimenpide = R.over(R.lensProp('virheet'),
      R.prepend(newVirhe), toimenpide);
  }
</script>

<div class="mb-5">
  <div class="flex py-4">
    <Datepicker
      label="Määräpäivä"
      {disabled}
      bind:model={toimenpide}
      lens={R.lensProp('deadline-date')}
      format={Maybe.fold('', Formats.formatDateInstant)}
      parse={Parsers.optionalParser(Parsers.parseDate)}
      transform={EM.fromNull}
      validators={schema['deadline-date']}
      i18n={$_} />
  </div>

  {#if !R.isEmpty(templates)}
    <div class="w-1/2 py-4">
      <Select
        label="Valitse asiakirjapohja"
        bind:model={toimenpide}
        lens={R.lensProp('template-id')}
        parse={Maybe.fromNull}
        format={formatTemplate}
        required={true}
        validation={schema.publish}
        items={R.pluck('id', templates)} />
    </div>
  {/if}
</div>

<H2 text="Virheet" />

<div class="w-1/2 py-4">
  <Select
      label="Lisää uusi virhe"
      bind:model={virhetyyppi}
      lens={R.lens(R.identity, R.identity)}
      inputValueParse={R.prop('id')}
      format={Locales.label($locale)}
      on:change={event => addVirhe(parseInt(event.target.value))}
      required={true}
      validation={schema.publish}
      items={virhetyypit} />
</div>

<div class="my-5">
  {#each toimenpide.virheet as virhe}
  <div class="my-5">
    <div>{formatVirhetyyppi(virhe['type-id'])}</div>
    <div>{virhe.description}</div>
  </div>
  {/each}

  {#if R.isEmpty(toimenpide.virheet)}
    Ei virheitä
  {/if}
</div>

<div class="w-1/2 py-4">
  <Select
      label="Vakavuus"
      bind:model={toimenpide}
      lens={R.lensProp('severity-id')}
      parse={Maybe.fromNull}
      format={formatSeverity}
      required={true}
      validation={schema.publish}
      items={R.pluck('id', severities)} />
</div>

