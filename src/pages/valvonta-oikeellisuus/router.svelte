<script>
  import Router from 'svelte-spa-router';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import NewToimenpide from './new-toimenpide';
  import ExistingToimenpide from './existing-toimenpide';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/valvonta/oikeellisuus';
  const routes = {
    '/all': Valvonnat,
    '/:versio/:id': Valvonta,
    '/:versio/:id/:toimenpide-id': ExistingToimenpide,
    '/:versio/:id/new/:type-id': NewToimenpide,
    '*': NotFound
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('valvonta-oikeellisuus')} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-oikeellisuus'} />
