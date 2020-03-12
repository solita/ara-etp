<script>
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/yritys';
  const routes = {
    '/new': NewYritys,
    '/:id': ExistingYritys
  };
</script>

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<div class="w-full min-h-3em">
  <FlashMessage module={'Yritys'} />
</div>
<Router {routes} {prefix} />
