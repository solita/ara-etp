<script>
  import * as R from 'ramda';
  import Router, { wrap } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/Yritykset';
  import Laatijat from './Laatijat';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import {
    flashMessageStore,
    currentUserStore
  } from '@/stores';

  const prefix = '/laatija';
  const routes = {
    '/all': Laatijat,
    '/laatijoidentuonti': LaatijaUpload,
    '/:id/yritykset': Yritykset
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<Router {routes} {prefix} />

<FlashMessage module={'Laatija'} />
