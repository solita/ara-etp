<script>
  import Router from 'svelte-spa-router';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import Kohde from './kohde';
  import NewKohde from './new-kohde';
  import NotFound from '@Pages/not-found/not-found';
  import Henkilo from './henkilo';
  import NewHenkilo from './new-henkilo';
  import Yritys from './yritys';
  import NewYritys from './new-yritys';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/valvonta/kaytto';
  const routes = {
    '/all': Valvonnat,
    '/new': NewKohde,
    '/:id/valvonta': Valvonta,
    '/:id/kohde': Kohde,
    '/:kohde-id/henkilo/new': NewHenkilo,
    '/:kohde-id/henkilo/:id': Henkilo,
    '/:kohde-id/yritys/new': NewYritys,
    '/:kohde-id/yritys/:id': Yritys,
    '*': NotFound
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('valvonta-kaytto')} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-kaytto'} />
