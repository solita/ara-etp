<script>
  import Router from 'svelte-spa-router';

  import ExistingEnergiatodistus from './ExistingEnergiatodistus';
  import NewEnergiatodistus from './NewEnergiatodistus';
  import Energiatodistukset from './Energiatodistukset';
  import Allekirjoitus from './Allekirjoitus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import ToolBar from '@Component/ToolBar/ToolBar';
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

<div class="w-full relative flex">
  <div class="w-5/6">
    <Router {routes} {prefix} />
  </div>
  <div class="sticky top-3em w-1/6 self-start flex justify-end">
    <ToolBar />
  </div>
</div>

<div class="w-full min-h-3em">
  <FlashMessage module={'Energiatodistus'} />
</div>
