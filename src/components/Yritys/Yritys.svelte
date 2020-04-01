<script>
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import Country from '@Component/Geo/Country';
  import { flashMessageStore, countryStore } from '@/stores';

  const prefix = '/yritys';
  const routes = {
    '/new': NewYritys,
    '/:id': ExistingYritys
  };
</script>

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<Country />

{#if $countryStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Yritys'} />
