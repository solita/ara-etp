<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';

  import { routes } from '@Component/routes';
  import { setupI18n } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Breadcrumb from '@Component/Breadcrumb/Breadcrumb';
  import Footer from '@Component/Footer/Footer';
  import * as UserUtils from '@Utility/user-utils';
  import { currentUserStore, errorStore } from '@/stores';

  import Tailwindcss from '@/Tailwindcss';

  setupI18n();
  UserUtils.fetchAndStoreUser();

  $: isAppLoading = !$currentUserStore && !$errorStore;
  $: isUnauthorizedOnFirstLoad =
    !$currentUserStore && $errorStore && $errorStore.statusCode === 401;
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
  <Login redirectTimeout={2000} />
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
