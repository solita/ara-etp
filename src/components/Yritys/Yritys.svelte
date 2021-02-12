<script>
  import Router from 'svelte-spa-router';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import Yritykset from './yritykset';
  import Laatijat from './laatijat';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/yritys';
  const routes = {
    '/new': NewYritys,
    '/all': Yritykset,
    '/:id': ExistingYritys,
    '/:id/laatijat': Laatijat,
    '*': NotFound
  };
</script>

<style type="text/postcss">
</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<Router {routes} {prefix} />

<FlashMessage module={'Yritys'} />
