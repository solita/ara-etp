<script>
  import Router from 'svelte-spa-router';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import NewToimepide from './new-toimenpide';
  import ExistingToimepide from './valvonta';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/valvonta/oikeellisuus';
  const routes = {
    '/all': Valvonnat,
    '/:id': Valvonta,
    '/:id/new': NewToimepide,
    '/:id/:toimenpide-id': ExistingToimepide,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('viesti')} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-oikeellisuus'} />
