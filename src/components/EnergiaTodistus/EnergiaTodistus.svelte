<script>
  import Router from 'svelte-spa-router';

  import ExistingEnergiaTodistus from './ExistingEnergiaTodistus';
  import NewEnergiaTodistus from './NewEnergiaTodistus';
  import Energiatodistukset from './Energiatodistukset';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import ToolBar from '@Component/ToolBar/ToolBar';
  import { flashMessageStore } from '@/stores';

  const prefix = '/energiatodistus';
  const routes = {
    '/all': Energiatodistukset,
    '/:version/new': NewEnergiaTodistus,
    '/:version/:id': ExistingEnergiaTodistus
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('EnergiaTodistus')} />

<div class="w-full relative flex">
  <div class="w-5/6">
    <Router {routes} {prefix} />
  </div>
  <div class="sticky top-3em w-1/6 self-start flex justify-end">
    <ToolBar />
  </div>
</div>

<div class="w-full min-h-3em">
  <FlashMessage module={'EnergiaTodistus'} />
</div>
