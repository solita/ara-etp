<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Either from '@Utility/either-utils';

  import { patevyydetStore } from '@/stores';

  const patevyydetFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/patevyydet/');

  Either.isRight($patevyydetStore) ||
    Future.fork(patevyydetStore.set, patevyydetStore.set, patevyydetFuture);
</script>
