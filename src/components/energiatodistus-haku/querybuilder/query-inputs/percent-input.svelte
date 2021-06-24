<script>
  import NumberInput from './number-input';
  import * as Parsers from '@Utility/parsers';

  import * as R from 'ramda';

  export let values = [];
  export let nameprefix;
  export let index = 0;

  const format = R.compose(num => num + ' %', R.multiply(100));

  const parse = R.compose(
    R.map(R.divide(R.__, 100)),
    Parsers.parseNumber,
    R.replace('%', '')
  );
</script>

<NumberInput {values} {nameprefix} {index} {format} {parse} />
