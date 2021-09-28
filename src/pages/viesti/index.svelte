<script>
  import Router from 'svelte-spa-router';

  import Viestit from './viestit';
  import NewKetju from './new-ketju';
  import ExistingKetju from './existing-ketju';
  import NotFound from '@Pages/not-found/not-found';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/viesti';
  const routes = {
    '/all': Viestit,
    '/new': NewKetju,
    '/:id': ExistingKetju,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('viesti')} />

<Router {routes} {prefix} />

<FlashMessage module={'viesti'} />
