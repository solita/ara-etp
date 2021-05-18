<script>
  import { onMount } from 'svelte';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  export let badgeFuture;

  let cancel = () => {};
  let badge = Maybe.None();

  const getBadgeContent = _ => {
    cancel();
    cancel = Future.fork(
      _ => {},
      value => (badge = Maybe.Some(value)),
      badgeFuture
    );
  };

  onMount(getBadgeContent);
</script>

<svelte:window on:hashchange={getBadgeContent} />

<slot {badge} />
