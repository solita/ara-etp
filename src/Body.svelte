<script>
  import Router from 'svelte-spa-router';
  import { location, replace } from 'svelte-spa-router';

  import { idTranslateStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import NavigationTabBar from '@Component/NavigationTabBar/NavigationTabBar';
  import Breadcrumb from '@Component/Breadcrumb/breadcrumb';
  import ResourceProvider from './resource-provider';
  import { buildRoutes } from '@Component/routes';

  export let whoami;
  export let config;

  const routes = buildRoutes(whoami);
</script>

<style type="text/postcss">
  .routecontainer {
    @apply w-full pb-10 relative;
  }
  .breadcrumbcontainer {
    @apply flex justify-center;
  }

  .breadcrumbcontainer {
    @apply bg-background;
  }

  .content {
    @apply flex flex-col items-center flex-grow py-8 px-10 mx-auto bg-light;
  }

  .content h1 :not(first) {
    @apply py-6;
  }
</style>

<ResourceProvider let:idTranslate>
  <div class="breadcrumbcontainer">
    <div class="xl:w-xl lg:w-lg md:w-md sm:w-sm">
      <Breadcrumb {whoami} {idTranslate} location={$location} />
    </div>
  </div>
  <section class="content xl:w-xl lg:w-lg md:w-md sm:w-sm">
    <div class="w-full">
      <NavigationTabBar
        {idTranslate}
        location={$location}
        {whoami}
        {config}
        i18n={$_} />
    </div>
    <div class="routecontainer">
      <Router on:conditionsFailed={_ => replace('/404')} {routes} />
    </div>
  </section>
</ResourceProvider>
