<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';
  import { location, link, replace } from 'svelte-spa-router';

  import * as Maybe from '@Utility/maybe-utils';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import { buildRoutes } from '@Component/routes';
  import { setupI18n, _ } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Breadcrumb from '@Component/Breadcrumb/Breadcrumb';
  import Footer from '@Component/Footer/Footer';
  import { currentUserStore, errorStore, idTranslateStore } from '@/stores';

  import CurrentKayttaja from '@Component/Kayttaja/CurrentKayttaja';

  import Tailwindcss from '@/Tailwindcss';
  import TableStyles from '@/TableStyles';

  setupI18n();

  $: isAppLoading = Maybe.isNone($currentUserStore) && !$errorStore;
  $: isUnauthorizedOnFirstLoad =
    Maybe.isNone($currentUserStore) &&
    $errorStore &&
    $errorStore.statusCode === 401;

  $: routes = R.compose(
    Maybe.orSome({}),
    R.map(buildRoutes)
  )($currentUserStore);
</script>

<style type="text/postcss">
  .appcontainer {
    @apply flex flex-col flex-grow justify-between w-full min-h-screen;
  }

  .routecontainer {
    @apply w-full pb-10 relative;
  }

  .content {
    @apply flex flex-col items-center flex-grow py-8 px-10 mx-auto bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<Tailwindcss />
<TableStyles />

<CurrentKayttaja />

{#if isAppLoading}
  <Loading />
{:else if isUnauthorizedOnFirstLoad}
  <Login redirectTimeout={2000} />
{:else}
  <div class="appcontainer">
    <Header />
    <Breadcrumb
      idTranslate={$idTranslateStore}
      location={$location}
      user={Maybe.get($currentUserStore)}
      i18n={$_} />
    <section class="content xl:w-xl lg:w-lg md:w-md sm:w-sm">
      <div class="w-full">
        <NavigationTabBar
          location={$location}
          user={Maybe.get($currentUserStore)}
          i18n={$_} />
      </div>
      <div class="routecontainer">
        <Router on:conditionsFailed={_ => replace('/404')} {routes} />
      </div>
    </section>
    <Footer />
  </div>
{/if}
