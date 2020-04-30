<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';

  import * as Maybe from '@Utility/maybe-utils';

  import * as Navigation from '@Utility/navigation';
  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import { buildRoutes } from '@Component/routes';
  import { setupI18n } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Breadcrumb from '@Component/Breadcrumb/Breadcrumb';
  import Footer from '@Component/Footer/Footer';
  import {
    currentUserStore,
    errorStore,
    breadcrumbStore,
    navigationStore
  } from '@/stores';

  import CurrentKayttaja from '@Component/Kayttaja/CurrentKayttaja';

  import Tailwindcss from '@/Tailwindcss';

  setupI18n();

  $: isAppLoading = Maybe.isNone($currentUserStore) && !$errorStore;
  $: isUnauthorizedOnFirstLoad =
    Maybe.isNone($currentUserStore) &&
    $errorStore &&
    $errorStore.statusCode === 401;

  $: links = R.compose(
    Maybe.orSome([{ text: '...', href: '' }]),
    R.map(Navigation.linksForKayttaja)
  )($currentUserStore);

  const routes = buildRoutes(breadcrumbStore);
</script>

<style type="text/postcss">
  .container {
    @apply flex flex-col justify-between mx-auto max-w-full min-h-screen;
  }

  .routecontainer {
    @apply flex-grow pb-10 relative max-w-1280;
  }

  .content {
    @apply flex flex-col flex-grow pb-8 px-20 pt-8 bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<Tailwindcss />

<CurrentKayttaja />

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
