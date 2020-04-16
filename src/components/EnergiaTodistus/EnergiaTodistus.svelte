<script>
  import Router from 'svelte-spa-router';

  import ExistingEnergiaTodistus from './ExistingEnergiaTodistus';
  import NewEnergiaTodistus from './NewEnergiaTodistus';
  import Energiatodistukset from './Energiatodistukset';
  import Allekirjoitus from './Allekirjoitus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/energiatodistus';
  const routes = {
    '/all': Energiatodistukset,
    '/:version/new': NewEnergiaTodistus,
    '/:version/:id': ExistingEnergiaTodistus,
    '/:version/:id/sign': Allekirjoitus
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('EnergiaTodistus')} />

<Router {routes} {prefix} />

<div class="w-full min-h-3em">
  <FlashMessage module={'EnergiaTodistus'} />
</div>
