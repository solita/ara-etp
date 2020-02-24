<script>
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';
  import { routes } from './components/routes.js';

  import { setupI18n, _, locale, locales, isLoading } from './i18n.js';

  import * as R from 'ramda';

  import Header from './components/Header/Header.svelte';
  import Breadcrumb from './components/Breadcrumb/Breadcrumb.svelte';
  import Footer from './components/Footer/Footer.svelte';

  import Tailwindcss from './Tailwindcss.svelte';

  import * as Future from './utils/future-utils';
  import * as ErrorUtils from './utils/error-utils';
  import * as UserUtils from './components/User/user-utils';
  import { currentUserStore, errorStore } from './stores.js'

  setupI18n();

  Future.fork(R.compose(
    errorStore.set,
    ErrorUtils.httpError({})
  ),
  currentUserStore.set,
  UserUtils.userFuture(fetch, 'current'));

  $: {
    if($errorStore && $errorStore.statusCode === 401 && !$currentUserStore) {
      window.location.href= 'http://example.com';
    }
  }

</script>

<style type="text/postcss">
  .container {
    @apply flex flex-col justify-around mx-auto max-w-full min-h-screen;
  }

  .routecontainer {
    @apply flex-grow py-10 px-20 bg-light;
  }
</style>

<Tailwindcss />
{#if !$currentUserStore}
  Loading...
{:else if $locale && !$isLoading}
  <div class="container">
    <Header />
    <Breadcrumb />
    <div class="routecontainer">
      <Router {routes} />
    </div>
    <Footer />
  </div>
{/if}
