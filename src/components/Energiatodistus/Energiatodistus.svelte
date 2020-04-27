<script>
  import Router from 'svelte-spa-router';

  import ExistingEnergiatodistus from './ExistingEnergiatodistus';
  import NewEnergiatodistus from './NewEnergiatodistus';
  import Energiatodistukset from './Energiatodistukset';
  import Allekirjoitus from './Allekirjoitus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/energiatodistus';
  const routes = {
    '/all': Energiatodistukset,
    '/:version/new': NewEnergiatodistus,
    '/:version/:id': ExistingEnergiatodistus,
    '/:version/:id/sign': Allekirjoitus
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em">
  <FlashMessage module={'Energiatodistus'} />
</div>
