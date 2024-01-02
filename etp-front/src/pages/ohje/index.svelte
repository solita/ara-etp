<script>
  import Router from 'svelte-spa-router';

  import OhjeViewer from './viewer';
  import OhjeEditor from './editor';
  import OhjeCreator from './creator';
  import OhjeDeleted from './deleted';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { announcementsForModule } from '@Utility/announce';

  const prefix = '/ohje';
  const routes = {
    '/new': OhjeCreator,
    '/deleted': OhjeDeleted,
    '/:id': OhjeViewer,
    '/:id/edit': OhjeEditor,
    '*': NotFound
  };

  const { clearAnnouncements } = announcementsForModule('ohje');
</script>

<svelte:window on:hashchange={clearAnnouncements} />

<Router {routes} {prefix} />

<FlashMessage module={'ohje'} />
