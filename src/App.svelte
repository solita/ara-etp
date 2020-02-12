<script>
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';
  import { routes } from './components/routes.js';

  import { setupI18n, _, locale, locales, isLoading } from './i18n.js';

  import Header from './components/Header/Header.svelte';
  import Breadcrumb from './components/Breadcrumb/Breadcrumb.svelte';
  import Footer from './components/Footer/Footer.svelte';

  import Tailwindcss from './Tailwindcss.svelte';

  setupI18n();
</script>

<style type="text/postcss">
  .container {
    @apply flex flex-col justify-center mx-auto max-w-6xl min-h-screen;
  }

  .routecontainer {
    @apply flex-grow;
  }
</style>

<Tailwindcss />

{#if $isLoading}
  Loading...
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
