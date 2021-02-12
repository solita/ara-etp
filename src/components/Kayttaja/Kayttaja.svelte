<script>
  import Router from 'svelte-spa-router';

  import Kayttajat from './kayttajat';
  import ExistingKayttaja from './ExistingKayttaja';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import { flashMessageStore } from '@/stores';

  const prefix = '/kayttaja';
  const routes = {
    '/all': Kayttajat,
    '/:id': ExistingKayttaja
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Kayttaja')} />

<Router {routes} {prefix} />
<FlashMessage module={'Kayttaja'} />
