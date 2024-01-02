<script>
  import Input from '@Component/Input/Input';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let values = [];
  export let nameprefix;
  export let index = 0;
  export let format = Formats.numberFormat;
  export let parse = Parsers.parseNumber;

  let value = R.defaultTo(Parsers.parseNumber('a'), R.head(values));
</script>

<div>
  <Input
    id={`${nameprefix}_value_${index}`}
    name={`${nameprefix}_value_${index}`}
    center={true}
    compact={true}
    lens={R.lens(R.identity, R.identity)}
    {format}
    {parse}
    model={value}
    i18n={$_} />
</div>
