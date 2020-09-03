<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import Yritykset from './Yritykset';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore, currentUserStore } from '@/stores';

  const prefix = '/yritys';
  const routes = {
    '/new': NewYritys,
    '/all': Yritykset,
    '/:id': ExistingYritys
  };
</script>

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<Router {routes} {prefix} />

<FlashMessage module={'Yritys'} />
