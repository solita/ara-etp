<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import { locale, _ } from '@Language/i18n';

  import * as formats from '@Utility/formats';

  import Input from '@Component/Input/Input';

  export let path;
  export let model;
  export let schema;
  export let required = false;
  export let disabled = false;
  export let compact = false;
  export let center = true;
  export let format = formats.optionalString;

  const index = R.compose(
    Maybe.fromNull,
    R.head,
    R.filter(item => typeof item === 'number')
  )(path);

  const nonArrayPath = R.map(
    R.when(item => typeof item === 'number', R.always(0)),
    path
  );

  const id = R.replace(/-fi|-sv/g, '', R.join('.', nonArrayPath));
  const lens = R.lensPath(path);

  const type = R.view(R.lensPath(nonArrayPath), schema);

  if (R.isNil(type)) {
    throw "Property: " + id + " does not exists in schema."
  }
</script>

<Input
  {id}
  name={id}
  label={`${R.compose( Maybe.orSome(''), R.map(i => `${i + 1}. `) )(index)}${$_('energiatodistus.' + id)}`}
  {required}
  {disabled}
  {compact}
  {center}
  bind:model
  {lens}
  format={type.format || format}
  parse={type.parse}
  validators={type.validators}
  i18n={$_} />
