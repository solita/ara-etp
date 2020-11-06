<script>
  import SimpleInput from '@Component/Input/SimpleInput';
  import * as parsers from '@Utility/parsers';
  import * as Either from '@Utility/either-utils';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import Select from '@Component/Select/Select';

  export let value;
  export let nameprefix;
  export let index;

  const luokat = ['A', 'B', 'C', 'D', 'E', 'F'];

  if (!R.includes(value, luokat)) {
    value = R.head(luokat);
  }
</script>

<Select
  name={`${nameprefix}_value_${index}`}
  allowNone={false}
  model={value}
  items={luokat}
  lens={R.identity} />
