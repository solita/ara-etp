<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/Yritykset';
  import ExistingLaatija from './ExistingLaatija';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import Country from '@Component/Geo/Country';
  import ToimintaAlueet from '@Component/Geo/ToimintaAlueet';
  import {
    flashMessageStore,
    toimintaAlueetStore,
    countryStore
  } from '@/stores';

  const prefix = '/laatija';
  const routes = {
    '/upload': LaatijaUpload,
    '/:id/yritykset': Yritykset,
    '/:id': ExistingLaatija
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<Country />
<ToimintaAlueet />

{#if $countryStore.isRight() && $toimintaAlueetStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Laatija'} />
