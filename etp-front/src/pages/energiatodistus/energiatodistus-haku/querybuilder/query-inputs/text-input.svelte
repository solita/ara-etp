<script>
  import Input from '@Component/Input/Input';
  import * as Validation from '@Utility/validation';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let index = 0;

  let value = R.defaultTo('', R.head(values));

  if (value === 'true' || value === 'false') {
    value = '';
  }
</script>

<Input
  id={`${nameprefix}_value_${index}`}
  name={`${nameprefix}_value_${index}`}
  center={true}
  compact={true}
  lens={R.lens(R.identity, R.identity)}
  model={value}
  validators={[Validation.isRequired]}
  i18n={$_} />
