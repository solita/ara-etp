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
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import Autocomplete from '@Component/Autocomplete/Autocomplete.svelte';
  import Input from '@Component/Input/Input.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.toimenpide.audit-report';
  const i18n = $_;
  const i18nLocale = $locale;

  export let toimenpide;
  export let templates;
  export let severities;
  export let virhetyypit;
  export let disabled;
  export let schema;
  export let dirty;

  let virheInEditMode = Maybe.None();

  $: formatTemplate = Locales.labelForId($locale, templates);
  $: formatVirhetyyppi = Locales.labelForId($locale, virhetyypit);
  $: formatSeverity = Locales.labelForId($locale, severities);

  $: toimenpideLocale = R.compose(
    R.map(R.prop('language')),
    R.chain(id => Maybe.findById(id, templates)),
    R.prop('template-id')
  );

  const addVirhe = R.forEach(tyyppi => {
    const locale = Maybe.orSome(i18nLocale, toimenpideLocale(toimenpide));

    const newVirhe = {
      'type-id': tyyppi.id,
      description: Locales.prop('description', locale, tyyppi)
    };

    toimenpide = R.over(
      R.lensProp('virheet'),
      R.prepend(newVirhe),
      toimenpide
    );

    dirty = true;
  });

  const removeVirhe = index => {
    toimenpide = R.over(R.lensProp('virheet'), R.remove(index, 1), toimenpide);
    dirty = true;
  };

  const edit = index => {
    virheInEditMode = Maybe.Some(index);
  };

  const close = _ => {
    virheInEditMode = Maybe.None();
  };

  $: newVirhetypes = R.filter(
    R.compose(
      R.not,
      R.includes(R.__, R.pluck('type-id', toimenpide.virheet)),
      R.prop('id')
    ),
    virhetyypit
  );

  const findVirhetyyppi = (name, virhetyypit) =>
    Maybe.find(
      R.compose(
        R.includes(R.toLower(name)),
        R.map(R.toLower),
        R.props(['label-fi', 'label-sv'])
      ),
      virhetyypit);

  const selectNewVirhe = event => {
    const newVirhetyyppi = findVirhetyyppi(event.target.value, virhetyypit);
    addVirhe(newVirhetyyppi);
    if (Maybe.isSome(newVirhetyyppi)) {
      const input = event.target;
      input.value = '';
      input.blur();
      input.focus();
    }
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
        {disabled}
        validation={schema.publish}
        items={R.pluck('id', templates)} />
    </div>
  {/if}
</div>

<H2 text="Virheet" />
{#if !disabled}
  <div class="w-1/2 py-4">
    <Autocomplete items={R.map(Locales.label($locale), newVirhetypes)}
                  size={10}>
      <Input
          id="add-virhe"
          name="add-virhe"
          label={i18n(i18nRoot + '.add-virhe')}
          required={false}
          {disabled}
          on:input={selectNewVirhe}
          search={true}
          {i18n} />
    </Autocomplete>
  </div>
{/if}

<div class="my-5">
  {#each toimenpide.virheet as virhe, index}
    <div class="my-5">
      <div class="flex mb-2">
        <div class="text-primary font-bold truncate">
          {formatVirhetyyppi(virhe['type-id'])}
        </div>
        {#if !disabled}
          <div class="ml-2 flex">
            {#if Maybe.exists(R.equals(index), virheInEditMode)}
              <button
                on:click|preventDefault|stopPropagation={close}
                class="flex items-center space-x-1 mx-1 text-primary text-sm">
                <span class="font-icon">close</span> Sulje muokkaus
              </button>
            {:else}
              <button
                on:click|preventDefault|stopPropagation={_ => edit(index)}
                class="flex items-center space-x-1 mx-1 text-primary text-sm">
                <span class="font-icon">edit</span> Muokkaa
              </button>
            {/if}
            <button
              on:click|preventDefault|stopPropagation={_ => removeVirhe(index)}
              class="flex items-center space-x-1 mx-1 text-error text-sm">
              <span class="font-icon">remove_circle</span> Poista
            </button>
          </div>
        {/if}
      </div>
      <div>
        {#if Maybe.exists(R.equals(index), virheInEditMode)}
          <Textarea
            id={'virhe.description'}
            name={'virhe.description'}
            label={i18n(i18nRoot + '.virhe.description')}
            bind:model={toimenpide}
            lens={R.compose(
              R.lensProp('virheet'),
              R.lensIndex(index),
              R.lensProp('description')
            )}
            required={true}
            parse={R.trim}
            compact={false}
            {i18n} />
        {:else}
          {virhe.description}
        {/if}
      </div>
    </div>
  {/each}

  {#if R.isEmpty(toimenpide.virheet)}
    Ei virheitä
  {/if}
</div>

<H2 text="Vakavuus" />

<div class="w-1/2 py-4">
  <Select
    label="Valitse vakavuus"
    bind:model={toimenpide}
    lens={R.lensProp('severity-id')}
    parse={Maybe.fromNull}
    format={formatSeverity}
    required={true}
    {disabled}
    validation={schema.publish}
    items={R.pluck('id', severities)} />
</div>
