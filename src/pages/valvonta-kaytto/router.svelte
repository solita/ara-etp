<script>
  import Router from 'svelte-spa-router';
  import { wrap } from 'svelte-spa-router/wrap';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import Kohde from './kohde';
  import NewKohde from './new-kohde';
  import NotFound from '@Pages/not-found/not-found';
  import Osapuoli from './osapuoli';
  import NewOsapuoli from './new-osapuoli';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const route = (type, component) => wrap({
    component: component,
    props: {
      type: type
    }
  });

  const prefix = '/valvonta/kaytto';
  const routes = {
    '/all': Valvonnat,
    '/new': NewKohde,
    '/:id/kohde': Kohde,
    '/:id/valvonta': Valvonta,
    '/:valvonta-id/henkilo/new': route('henkilo', NewOsapuoli),
    '/:valvonta-id/henkilo/:id': route('henkilo', Osapuoli),
    '/:valvonta-id/yritys/new': route('yritys', NewOsapuoli),
    '/:valvonta-id/yritys/:id': route('yritys', Osapuoli),
    '*': NotFound
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('valvonta-kaytto')} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-kaytto'} />
