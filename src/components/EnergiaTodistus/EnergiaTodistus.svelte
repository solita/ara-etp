<script>
  import Router from 'svelte-spa-router';

  import ExistingEnergiaTodistus from './ExistingEnergiaTodistus';
  import NewEnergiaTodistus from './NewEnergiaTodistus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/energiatodistus';
  const routes = {
    '/:year/new': NewEnergiaTodistus,
    '/:year/:id': ExistingEnergiaTodistus
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('EnergiaTodistus')} />


<div class="w-full min-h-3em">
  <FlashMessage module={'EnergiaTodistus'} />
</div>
<Router {routes} {prefix} />
