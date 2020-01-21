<script>
  import Router from 'svelte-spa-router';
  import { link } from 'svelte-spa-router';
  import { routes } from './components/routes.js';

  import { setupI18n, _, locale, locales, isLoading } from './i18n.js';

  import Tailwindcss from './Tailwindcss.svelte';

  setupI18n();
</script>

<style type="text/postcss">
  .container {
    @apply flex justify-center;
  }
</style>

<Tailwindcss />


{#if $isLoading}
  Loading...
{:else} 
<div class="container">
  <Router {routes} />
</div>
{/if}