<script>
  import * as R from 'ramda';
  import OperatorInput from './operator-input';

  export let model;
  export let lens;
  export let operators;

  let value = R.view(lens, model);

  $: model = R.compose(
    R.set(lens, R.__, model),
    R.over(
      R.lensIndex(2),
      R.when(
        R.compose(
          R.equals('String'),
          R.type
        ),
        R.compose(
          parseFloat,
          R.replace(/,/g, '.')
        )
      )
    )
  )(value);
</script>

<style>

</style>

<OperatorInput bind:model={value} lens={R.identity} {operators} />
