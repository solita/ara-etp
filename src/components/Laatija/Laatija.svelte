<script>
  import * as R from 'ramda';
  import Router from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/Yritykset';
  import ExistingLaatija from './ExistingLaatija';
  import Laatijat from './Laatijat';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import Country from '@Component/Geo/Country';
  import Patevyydet from './Patevyydet';
  import ToimintaAlueet from '@Component/Geo/ToimintaAlueet';
  import {
    flashMessageStore,
    toimintaAlueetStore,
    countryStore,
    navigationStore,
    patevyydetStore
  } from '@/stores';

  const prefix = '/laatija';
  const routes = {
    '/all': Laatijat,
    '/upload': LaatijaUpload,
    '/:id/yritykset': Yritykset,
    '/:id': ExistingLaatija
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<Country />
<ToimintaAlueet />
<Patevyydet />

{#if $countryStore.isRight() && $toimintaAlueetStore.isRight() && $patevyydetStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Laatija'} />
