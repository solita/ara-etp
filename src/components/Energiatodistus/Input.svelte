<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import { locale, _ } from '@Language/i18n';
  import * as inputs from './inputs';
  import * as formats from '@Utility/formats';

  import Input from '@Component/Input/Input';

  export let path;
  export let model;
  export let schema;
  export let required = false;
  export let disabled = false;
  export let compact = false;
  export let center = true;
  export let search = false;
  export let format = formats.optionalString;
  export let inputLanguage = Maybe.None();

  const id = inputs.id(path);
  const type = inputs.type(schema, path);
</script>

<Input
  {id}
  name={id}
  label={compact ?
    inputs.fullLabel($_, inputLanguage, path) :
    inputs.label($_, inputLanguage, path)}
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
  i18n={$_} />
