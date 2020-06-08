<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as et from './energiatodistus-utils';
  import * as formats from '@Utility/formats';

  import Textarea from '@Component/Textarea/Textarea';

  export let path;
  export let model;
  export let schema;
  export let required = false;
  export let disabled = false;
  export let compact = false;
  export let format = formats.optionalString;

  const id = R.replace(/-fi|-sv/g, '', R.join('.', path));
  const lens = R.lensPath(path);
  const type = R.view(lens, schema);
</script>

<Textarea
  {id}
  name={id}
  label={$_('energiatodistus.' + id)}
  {required}
  {disabled}
  {compact}
  bind:model
  {lens}
  format={type.format || format}
  parse={type.parse}
  validators={type.validators}
  i18n={$_} />
