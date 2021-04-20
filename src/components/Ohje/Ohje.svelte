<script>
  import Router from 'svelte-spa-router';

  import OhjeViewer from './viewer';
  import OhjeEditor from './editor';
  import OhjeCreator from './creator';
  import Navigation from './navigation';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/ohje';
  const routes = {
    '/new': OhjeCreator,
    '/:id': OhjeViewer,
    '/:id/edit': OhjeEditor,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('ohje')} />
<div class="flex space-x-4">
  <div class="w-2/6 max-w-xs">
    <Navigation />
  </div>
  <div class="w-4/6 flex-grow">
    <Router {routes} {prefix} />
  </div>
</div>

<FlashMessage module={'ohje'} />
