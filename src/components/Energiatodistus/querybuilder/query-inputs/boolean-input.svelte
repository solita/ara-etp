<script>
  import * as R from 'ramda';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import { _ } from '@Language/i18n';

  export let model;
  export let lens;

  export let operators;

  const paramLens = R.compose(
    lens,
    R.lens(
      R.compose(
        R.equals('true'),
        R.nth(2)
      ),
      (v, m) => R.update(2, R.toString(v), m)
    )
  );
</script>

<style>

</style>

<Checkbox bind:model lens={paramLens} />
