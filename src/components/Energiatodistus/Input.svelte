<script>
  import * as Maybe from '@Utility/maybe-utils';

  import { _ } from '@Language/i18n';
  import * as inputs from './inputs';
  import * as formats from '@Utility/formats';

  import Input from '@Component/Input/Input';

  export let path;
  export let model;
  export let schema;
  export let disabled = false;
  export let compact = false;
  export let center = true;
  export let search = false;
  export let format = formats.optionalString;
  export let inputLanguage = Maybe.None();
  export let unit;
  export let labelUnit;

  const id = inputs.id(path);
  const type = inputs.type(schema, path);
  $: required = inputs.required(inputLanguage, type, model);
</script>

<style>
  .unit {
    @apply border-b-3 border-transparent py-1;
  }

  .disabled {
    @apply border-0 pb-4 pt-0;
  }
</style>

<div class="flex flex-row items-end">
  <div class="flex-grow">
    <Input
      {id}
      name={id}
      label={compact
        ? inputs.fullLabel($_, inputLanguage, path)
        : inputs.label($_, inputLanguage, path)}
      {required}
      {disabled}
      {compact}
      {center}
      {search}
      bind:model
      lens={inputs.dataLens(inputLanguage, path)}
      format={type.format || format}
      parse={type.parse}
      validators={type.validators}
      warnValidators={type.warnValidators}
      i18n={$_}
      {labelUnit} />
  </div>
  <div class="unit" class:disabled>
    <svelte:component this={unit} />
  </div>
</div>
