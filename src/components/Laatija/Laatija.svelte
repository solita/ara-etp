<script>
  import Router, { wrap } from 'svelte-spa-router';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/yritykset';
  import Laatijat from './Laatijat';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/laatija';
  const routes = {
    '/all': Laatijat,
    '/laatijoidentuonti': LaatijaUpload,
    '/:id/yritykset': Yritykset,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<Router {routes} {prefix} />

<FlashMessage module={'Laatija'} />
