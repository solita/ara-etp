<script>
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';
  import { routes } from './components/routes.js';

  import { setupI18n, _, locale, locales, isLoading } from './i18n.js';

  import Header from './components/Header/Header.svelte';
  import Breadcrumb from './components/Breadcrumb/Breadcrumb.svelte';
  import Footer from './components/Footer/Footer.svelte';

  import Tailwindcss from './Tailwindcss.svelte';

  import { fetchUser, currentUser } from './components/User/user';

  setupI18n();

  currentUser.set(fetchUser('current'));
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

{#if $locale && !$isLoading}
  <div class="container">
    <Header />
    <Breadcrumb />
    <div class="routecontainer">
      <Router {routes} />
    </div>
    <Footer />
  </div>
{/if}
