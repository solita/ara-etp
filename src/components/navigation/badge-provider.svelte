<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  export let badgeFuture;

  let cancel = () => {};
  let badge = Maybe.None();

  const getBadgeContent = _ => {
    cancel = R.compose(
      Future.fork(
        _ => {},
        value => (badge = Maybe.Some(value))
      ),
      R.chain(R.always(badgeFuture)),
      Future.after(200),
      R.tap(cancel)
    )(true);
  };

  onMount(getBadgeContent);
</script>

<svelte:window on:hashchange={getBadgeContent} />

<slot {badge} />
