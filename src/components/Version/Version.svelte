<script>
  import * as R from 'ramda';

  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';

  let versionInfo = Maybe.None();

  R.compose(
    Future.fork(e => e, v => (versionInfo = v)),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(`/build/version.json?${Date.now()}`);
</script>

<style>

</style>

{versionInfo.version}
