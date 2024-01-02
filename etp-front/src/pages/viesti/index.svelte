<script>
  import Router from 'svelte-spa-router';

  import Viestit from './viestit';
  import NewKetju from './new-ketju';
  import ExistingKetju from './existing-ketju';
  import Liitteet from './liitteet';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { announcementsForModule } from '@Utility/announce';

  const { clearAnnouncements } = announcementsForModule('viesti');
  const prefix = '/viesti';
  const routes = {
    '/all': Viestit,
    '/new': NewKetju,
    '/:id': ExistingKetju,
    '/:id/liitteet': Liitteet,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />

<FlashMessage module={'viesti'} />
