<script>
  import Router from 'svelte-spa-router';
  import { location, replace } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import NavigationTabBar from '@Pages/navigation/navigation-tab-bar';
  import Breadcrumb from '@Pages/breadcrumb/breadcrumb';
  import ResourceProvider from './resource-provider';
  import { buildRoutes } from '@Pages/routes';

  export let whoami;
  export let config;

  const routes = buildRoutes(whoami);
</script>

<style type="text/postcss">
  #routecontainer {
    @apply relative w-full max-w-1440 pb-10;
  }
  #breadcrumbcontainer {
    @apply mx-auto w-full bg-background;
  }

  .content {
    @apply mx-auto flex w-full max-w-1440 flex-grow flex-col items-center bg-light py-8;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<ResourceProvider let:idTranslate>
  <nav id="breadcrumbcontainer" aria-label={$_('navigation.breadcrumb-bar')}>
    <div class="mx-auto w-full max-w-1440 px-2 lg:px-10">
      <Breadcrumb {whoami} {idTranslate} location={$location} />
    </div>
  </nav>
  <div class="content px-2 lg:px-10">
    <nav
      id="navigationcontainer"
      class="w-full pb-4"
      aria-label={$_('navigation.navigation-tab-bar')}>
      <NavigationTabBar
        {idTranslate}
        location={$location}
        {whoami}
        {config}
        i18n={$_} />
    </nav>
    <main id="routecontainer">
      <Router on:conditionsFailed={_ => replace('/404')} {routes} />
    </main>
  </div>
</ResourceProvider>
