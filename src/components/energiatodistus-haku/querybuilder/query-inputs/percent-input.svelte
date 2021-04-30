<script>
  import NumberInput from './number-input';
  import * as Formats from '@Utility/formats';
  import * as Parsers from '@Utility/parsers';

  import * as R from 'ramda';

  export let values = [];
  export let nameprefix;
  export let index = 0;

  const parse = R.compose(R.map(R.divide(R.__, 100)), Parsers.parseNumber);
</script>

<NumberInput
  {values}
  {nameprefix}
  {index}
  format={Formats.percentFormat}
  {parse} />
