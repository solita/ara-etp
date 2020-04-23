<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as et from './energiatodistus-utils';

  import Input from '@Component/Input/Input';

  export let path;
  export let model;
  export let schema;
  export let required = false;
  export let disabled = false;
  export let compact = false;

  const id = R.replace(/-fi|-sv/g, '', R.join('.', path));
  const lens = R.lensPath(path);
  const type = R.view(lens, schema);
</script>

<Input
  {id}
  name={id}
  label={$_('energiatodistus.' + id)}
  {required}
  {disabled}
  {compact}
  bind:model
  {lens}
  format={et.formatters.optionalText}
  parse={type.parse}
  validators={type.validators}
  i18n={$_} />
