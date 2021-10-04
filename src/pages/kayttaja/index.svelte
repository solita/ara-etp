<script>
  import Router from 'svelte-spa-router';

  import Kayttajat from './kayttajat';
  import ExistingKayttaja from './existing-kayttaja';
  import NewKayttaja from './new-kayttaja';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';

  import { flashMessageStore } from '@/stores';

  const prefix = '/kayttaja';
  const routes = {
    '/all': Kayttajat,
    '/new': NewKayttaja,
    '/:id': ExistingKayttaja
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('kayttaja')} />

<Router {routes} {prefix} />
<FlashMessage module={'kayttaja'} />
