<script>
  import Router from 'svelte-spa-router';
  import { location, replace } from 'svelte-spa-router';

  import { idTranslateStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import NavigationTabBar from '@Component/navigation/navigation-tab-bar';
  import Breadcrumb from '@Component/Breadcrumb/breadcrumb';
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
    @apply w-full max-w-1440 flex flex-col items-center flex-grow py-8 px-10 mx-auto bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<ResourceProvider let:idTranslate>
  <div id="breadcrumbcontainer">
    <div class="w-full max-w-1440 mx-auto px-10">
      <Breadcrumb {whoami} {idTranslate} location={$location} />
    </div>
  </div>
  <section class="content">
    <div id="navigationcontainer" class="w-full">
      <NavigationTabBar
        {idTranslate}
        location={$location}
        {whoami}
        {config}
        i18n={$_} />
    </div>
    <div id="routecontainer">
      <Router on:conditionsFailed={_ => replace('/404')} {routes} />
    </div>
  </section>
</ResourceProvider>
