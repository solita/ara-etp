<script>
  import Router from 'svelte-spa-router';

  import ExistingKayttaja from './ExistingKayttaja';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import {
    flashMessageStore
  } from '@/stores';

  const prefix = '/kayttaja';
  const routes = {
    '/:id': ExistingKayttaja
  };

</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Kayttaja')} />

<Router {routes} {prefix} />
<FlashMessage module={'Kayttaja'} />
