<script>
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';
  import { routes } from './components/routes.js';
  import { setupI18n, isLoading } from './i18n.js';
  import * as R from 'ramda';
  import Header from './components/Header/Header.svelte';
  import Loading from './components/Loading/Loading.svelte';
  import Login from './components/Login/Login.svelte';
  import Breadcrumb from './components/Breadcrumb/Breadcrumb.svelte';
  import Footer from './components/Footer/Footer.svelte';
  import * as UserUtils from './utils/user-utils'
  import { currentUserStore, errorStore } from './stores'

  import Tailwindcss from './Tailwindcss.svelte';

  setupI18n();
  UserUtils.fetchAndStoreUser();

  $: isAppLoading = $isLoading || !$currentUserStore && !$errorStore;
  $: isUnauthorizedOnFirstLoad = !$currentUserStore &&
                                 $errorStore &&
                                 $errorStore.statusCode === 401;
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

{#if isAppLoading}
  <Loading />
{:else if isUnauthorizedOnFirstLoad}
  <Login />
{:else}
  <div class="container">
    <Header />
    <Breadcrumb />
    <div class="routecontainer">
      <Router {routes} />
    </div>
    <Footer />
  </div>
{/if}
