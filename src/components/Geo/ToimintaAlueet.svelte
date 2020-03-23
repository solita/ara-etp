<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Either from '@Utility/either-utils';

  import { toimintaAlueetStore } from '@/stores';

  const toimintaAlueetFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/toimintaalueet/');

  Either.isRight($toimintaAlueetStore) ||
    Future.fork(
      toimintaAlueetStore.set,
      toimintaAlueetStore.set,
      toimintaAlueetFuture
    );
</script>
