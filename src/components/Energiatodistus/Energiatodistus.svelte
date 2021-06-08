<script>
  import Router from 'svelte-spa-router';

  import EnergiatodistusResolver from './energiatodistus-resolver';
  import ExistingEnergiatodistus from './ExistingEnergiatodistus';
  import NewEnergiatodistus from './NewEnergiatodistus';
  import Energiatodistukset from './energiatodistukset';
  import Liitteet from './liitteet';
  import Viestit from './viestit';
  import NewKetju from './new-ketju';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';
  import NotFound from '@Component/NotFound/NotFound';

  const prefix = '/energiatodistus';
  const routes = {
    '/all': Energiatodistukset,
    '/:id': EnergiatodistusResolver,
    '/:version/new': NewEnergiatodistus,
    '/:version/:id': ExistingEnergiatodistus,
    '/:version/:id/liitteet': Liitteet,
    '/:version/:id/viestit': Viestit,
    '/:version/:id/viestit/new': NewKetju,
    '*': NotFound
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em sticky bottom-0">
  <FlashMessage module={'Energiatodistus'} />
</div>
