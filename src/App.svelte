<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import { routes } from '@Component/routes';
  import { setupI18n } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Breadcrumb from '@Component/Breadcrumb/Breadcrumb';
  import Footer from '@Component/Footer/Footer';
  import * as UserUtils from '@Utility/user-utils';
  import { currentUserStore, errorStore, breadcrumbStore } from '@/stores';

  import Tailwindcss from '@/Tailwindcss';

  setupI18n();
  UserUtils.fetchAndStoreUser();

  $: isAppLoading = !$currentUserStore && !$errorStore;
  $: isUnauthorizedOnFirstLoad =
    !$currentUserStore && $errorStore && $errorStore.statusCode === 401;

  let links = [
    {
      text: '...'
    }
  ];
</script>

<style type="text/postcss">
  .container {
    @apply flex flex-col justify-between mx-auto max-w-full min-h-screen;
  }

  .routecontainer {
    @apply flex-grow pb-10;
  }

  .content {
    @apply flex flex-col flex-grow pb-8 px-20 pt-8 bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
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
    <Breadcrumb value={$breadcrumbStore} />
    <section class="content">
      <div class="w-full">
        <NavigationTabBar {links} />
      </div>
      <div class="routecontainer">
        <Router {routes} />
      </div>
    </section>
    <Footer />
  </div>
{/if}
