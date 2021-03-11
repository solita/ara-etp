<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as inputs from './inputs';
  import * as formats from '@Utility/formats';
  import * as Maybe from '@Utility/maybe-utils';

  import Textarea from '@Component/Textarea/Textarea';

  export let path;
  export let model;
  export let schema;
  export let disabled = false;
  export let compact = false;
  export let format = formats.optionalString;
  export let inputLanguage = Maybe.None();

  const id = inputs.id(path);
  $: type = inputs.type(schema, path);
  $: required = inputs.required(inputLanguage, type, model);
</script>

<Textarea
  {id}
  name={id}
  label={inputs.label($_, inputLanguage, path)}
  {required}
  {disabled}
  {compact}
  bind:model
  lens={inputs.dataLens(inputLanguage, path)}
  format={type.format || format}
  parse={type.parse}
  validators={R.defaultTo([], type.validators)}
  i18n={$_} />
