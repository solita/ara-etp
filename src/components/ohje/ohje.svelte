<script>
  import Router from 'svelte-spa-router';

  import OhjeViewer from './viewer';
  import OhjeEditor from './editor';
  import OhjeCreator from './creator';
  import OhjeDeleted from './deleted';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/ohje';
  const routes = {
    '/new': OhjeCreator,
    '/deleted': OhjeDeleted,
    '/:id': OhjeViewer,
    '/:id/edit': OhjeEditor,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('ohje')} />

<Router {routes} {prefix} />

<FlashMessage module={'ohje'} />
