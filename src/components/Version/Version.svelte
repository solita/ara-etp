<script>
  import * as R from 'ramda';

  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';

  let versionInfo = Maybe.None();

  R.compose(
    Future.fork(e => e, v => (versionInfo = Maybe.Some(v))),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )(`version.json?${Date.now()}`);
</script>

<style>

</style>

{R.compose(Maybe.orSome(''), R.map(R.prop('version')))(versionInfo)}
