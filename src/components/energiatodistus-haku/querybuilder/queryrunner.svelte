<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';

  export let where;
  export let query;
  export let runQuery;

  $: R.compose(
    runQuery,
    R.assoc('where', EtHakuUtils.convertWhereToQuery(where))
  )(query);
</script>
