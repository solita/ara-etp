<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtHakuUtils from '@Component/Energiatodistus/energiatodistus-haku-utils';

  export let where;
  export let query;
  export let runQuery;

  const operations = EtHakuUtils.operations();

  const toQueryParameter = ([operation, key, ...values]) =>
    R.compose(
      R.map(op => R.apply(op.command, [key, ...values])),
      Maybe.fromNull,
      R.find(
        R.compose(
          R.equals(operation),
          R.prop('operation')
        )
      )
    )(operations);

  $: R.compose(
    runQuery,
    R.assoc(
      'where',
      R.compose(
        R.filter(R.length),
        R.map(
          R.compose(
            R.map(Maybe.get),
            R.filter(Maybe.isSome),
            R.map(toQueryParameter)
          )
        )
      )(where)
    )
  )(query);
</script>
