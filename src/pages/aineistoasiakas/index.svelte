<script>
  import Router from 'svelte-spa-router';

  import NewAineistoasiakas from './new-aineistoasiakas';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import { flashMessageStore } from '@/stores';

  const prefix = '/aineistoasiakas';
  const routes = {
    '/new': NewAineistoasiakas
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('aineistoasiakas')} />

<Router {routes} {prefix} />
<FlashMessage module={'aineistoasiakas'} />
