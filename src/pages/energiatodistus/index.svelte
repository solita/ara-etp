<script>
  import Router from 'svelte-spa-router';
  import wrap from 'svelte-spa-router/wrap';

  import EnergiatodistusResolver from './energiatodistus-resolver';
  import ExistingEnergiatodistus from './existing-energiatodistus';
  import NewEnergiatodistus from './new-energiatodistus';
  import Energiatodistukset from './energiatodistukset';
  import Liitteet from './liitteet';
  import Viestit from './viestit';
  import NewKetju from './new-ketju';
  import { flashMessageStore } from '@/stores';
  import Muutoshistoria from './muutoshistoria';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import NotFound from '@Component/NotFound/NotFound';

  const prefix = '/energiatodistus';
  const routes = {
    '/all': Energiatodistukset,
    '/:id': EnergiatodistusResolver,
    '/:id/viestit': EnergiatodistusResolver,
    '/:version/new': NewEnergiatodistus,
    '/:version/:id': ExistingEnergiatodistus,
    '/:version/:id/liitteet': Liitteet,
    '/:version/:id/viestit': Viestit,
    '/:version/:id/viestit/new': NewKetju,
    '/:version/:id/muutoshistoria': Muutoshistoria,
    '*': NotFound
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em sticky bottom-0">
  <FlashMessage module={'Energiatodistus'} />
</div>
