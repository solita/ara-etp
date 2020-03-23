<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Either from '@Utility/either-utils';

  import { countryStore } from '@/stores';

  const countryFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/countries/');

  Either.isRight($countryStore) ||
    Future.fork(countryStore.set, countryStore.set, countryFuture);
</script>
