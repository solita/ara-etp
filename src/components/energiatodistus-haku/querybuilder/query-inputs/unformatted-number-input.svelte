<script>
  import Input from '@Component/Input/Input';
  import * as Parsers from '@Utility/parsers';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let index = 0;

 let value = R.defaultTo(Parsers.parseNumber('a'), R.head(values));
</script>

<div>
  <Input
    id={`${nameprefix}_value_${index}`}
    name={`${nameprefix}_value_${index}`}
    center={true}
    compact={true}
    lens={R.lens(R.identity, R.identity)}
    parse={Parsers.parseNumber}
    model={value}
    i18n={$_} />
</div>
