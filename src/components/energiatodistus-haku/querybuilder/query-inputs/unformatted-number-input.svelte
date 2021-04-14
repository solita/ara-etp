<script>
  import NumberInput from './number-input';

  import * as R from 'ramda';

  export let values = [];
  export let nameprefix;
  export let index = 0;
</script>

<NumberInput {values} {nameprefix} {index} format={R.identity} />
