<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/Yritykset';
  import ExistingLaatija from './ExistingLaatija';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';

  import * as LaatijaUtils from './laatija-utils';

  const prefix = '/laatija';
  const routes = {
    '/upload': LaatijaUpload,
    '/:id/yritykset': Yritykset,
    '/:id': ExistingLaatija
  };
</script>

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<div class="w-full min-h-3em">
  <FlashMessage module={'Laatija'} />
</div>
<Router {routes} {prefix} />
