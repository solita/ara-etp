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

  const versiot = [2018, 2013];

  if (!R.includes(value, versiot)) {
    value = R.head(versiot);
  }
</script>

<Select
  name={`${nameprefix}_value_${index}`}
  allowNone={false}
  model={value}
  items={versiot}
  lens={R.identity} />
