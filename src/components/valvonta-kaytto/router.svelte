<script>
  import Router from 'svelte-spa-router';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import Kohde from './kohde';
  import NewKohde from './new-kohde';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/valvonta/kaytto';
  const routes = {
    '/all': Valvonnat,
    '/new': NewKohde,
    '/:id/valvonta': Valvonta,
    '/:id/kohde': Kohde,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('valvonta-kaytto')} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-kaytto'} />
