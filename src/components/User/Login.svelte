<script>
  import * as R from 'ramda';
  import { None } from '../../utils/maybe-utils';
  import * as Future from '../../utils/future-utils';
  import * as ErrorUtils from '../../utils/error-utils';
  import * as UserUtils from './user-utils';
  import { currentUserStore, errorStore } from '../../stores.js'

  export let storyBookLoggedIn = None();

  Future.fork(R.compose(
    errorStore.set,
    ErrorUtils.httpError({})
  ),
  currentUserStore.set,
  UserUtils.userFuture(fetch, 'current'))

  $: unauthorizedOnFirstLoad = !$currentUserStore &&
                               $errorStore &&
                               $errorStore.statusCode === 401

  $: {
    if(unauthorizedOnFirstLoad) {
      setTimeout(() => window.location.href = 'http://example.com', 3000);
    }
  }
</script>

<style type="text/postcss">
</style>

{#if storyBookLoggedIn.orNull() === false || unauthorizedOnFirstLoad}
<span>Odota hetki, sinut ohjataan kirjautumissivulle...</span>
{/if}
