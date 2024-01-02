<script>
  import Router from 'svelte-spa-router';

  import Valvonnat from './valvonnat';
  import Valvonta from './valvonta';
  import NewToimenpide from './new-toimenpide';
  import ExistingToimenpide from './existing-toimenpide';
  import Virhetypes from './virhetypes/index.svelte';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { announcementsForModule } from '@Utility/announce';

  const { clearAnnouncements } = announcementsForModule(
    'valvonta-oikeellisuus'
  );

  const prefix = '/valvonta/oikeellisuus';
  const routes = {
    '/all': Valvonnat,
    '/virhetypes': Virhetypes,
    '/:versio/:id': Valvonta,
    '/:versio/:id/:toimenpide-id': ExistingToimenpide,
    '/:versio/:id/new/:type-id': NewToimenpide,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />

<FlashMessage module={'valvonta-oikeellisuus'} />
