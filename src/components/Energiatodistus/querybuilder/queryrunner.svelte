<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtHakuUtils from '@Component/Energiatodistus/energiatodistus-haku-utils';

  export let where;
  export let query;

  const operations = EtHakuUtils.operations();

  const toQueryParameter = ([operation, key, ...values]) =>
    R.compose(
      R.map(
        R.compose(
          R.split(' '),
          op => R.apply(op.command, [key, ...values])
        )
      ),
      Maybe.fromNull,
      R.find(
        R.compose(
          R.equals(operation),
          R.prop('operation')
        )
      )
    )(operations);

  $: query = R.compose(
    JSON.stringify,
    R.filter(R.length),
    R.map(R.map(Maybe.get)),
    R.map(R.filter(Maybe.isSome)),
    R.map(R.map(toQueryParameter))
  )(where);

  $: console.log(`generated query:  ${query}`);
</script>

<style>

</style>
