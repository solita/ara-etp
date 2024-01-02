<script>
  import Router from 'svelte-spa-router';

  import NewYritys from './new-yritys';
  import ExistingYritys from './existing-yritys';
  import Yritykset from './yritykset';
  import Laatijat from './laatijat';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { announcementsForModule } from '@Utility/announce';

  const { clearAnnouncements } = announcementsForModule('yritys');
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

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />

<FlashMessage module={'yritys'} />
