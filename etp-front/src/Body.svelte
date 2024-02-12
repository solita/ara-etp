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
    @apply w-full pb-10 relative max-w-1440;
  }
  #breadcrumbcontainer {
    @apply w-full mx-auto bg-background;
  }

  .content {
    @apply w-full max-w-1440 flex flex-col items-center flex-grow py-8 mx-auto bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<ResourceProvider let:idTranslate>
  <nav id="breadcrumbcontainer" aria-label={$_('navigation.breadcrumb-bar')}>
    <div class="w-full max-w-1440 mx-auto px-2 lg:px-10">
      <Breadcrumb {whoami} {idTranslate} location={$location} />
    </div>
  </nav>
  <div class="content px-2 lg:px-10">
    <nav
      id="navigationcontainer"
      class="w-full"
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
