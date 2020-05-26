<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';
  import { location, link, replace } from 'svelte-spa-router';

  import * as Maybe from '@Utility/maybe-utils';

  import * as Navigation from '@Utility/navigation';
  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import { buildRoutes } from '@Component/routes';
  import { setupI18n, _ } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Breadcrumb from '@Component/Breadcrumb/Breadcrumb';
  import Footer from '@Component/Footer/Footer';
  import { currentUserStore, errorStore, navigationStore } from '@/stores';

  import CurrentKayttaja from '@Component/Kayttaja/CurrentKayttaja';

  import Tailwindcss from '@/Tailwindcss';
  import TableStyles from '@/TableStyles';

  setupI18n();

  $: isAppLoading = Maybe.isNone($currentUserStore) && !$errorStore;
  $: isUnauthorizedOnFirstLoad =
    Maybe.isNone($currentUserStore) &&
    $errorStore &&
    $errorStore.statusCode === 401;

  $: R.compose(
    navigationStore.set,
    Maybe.orSome([{ text: '...', href: '' }]),
    R.map(Navigation.linksForKayttaja)
  )($currentUserStore);

  $: routes = R.compose(
    Maybe.orSome({}),
    R.map(buildRoutes)
  )($currentUserStore);

  $: console.log(routes);
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
<TableStyles />

<CurrentKayttaja />

{#if isAppLoading}
  <Loading />
{:else if isUnauthorizedOnFirstLoad}
  <Login redirectTimeout={2000} />
{:else}
  <div class="container">
    <Header />
    <Breadcrumb location={$location} user={$currentUserStore} i18n={$_} />
    <section class="content">
      <div class="w-full">
        <NavigationTabBar links={$navigationStore} />
      </div>
      <div class="routecontainer">
        <Router on:conditionsFailed={_ => replace('/404')} {routes} />
      </div>
    </section>
    <Footer />
  </div>
{/if}
