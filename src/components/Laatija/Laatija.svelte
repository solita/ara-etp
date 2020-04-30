<script>
  import * as R from 'ramda';
  import Router, { wrap } from 'svelte-spa-router';

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
    patevyydetStore,
    breadcrumbStore
  } from '@/stores';

  const idFromDetails = R.compose(
    R.nth(1),
    R.tail,
    R.split('/'),
    R.prop('location')
  );

  const prefix = '/laatija';
  const routes = {
    '/all': wrap(Laatijat, _ => {
      breadcrumbStore.set([{ label: 'Laatijat', url: `#${prefix}/all` }]);
      return true;
    }),
    '/upload': wrap(LaatijaUpload, _ => {
      breadcrumbStore.set([
        { label: 'Laatijoiden tuonti', url: `#${prefix}/upload` }
      ]);
      return true;
    }),
    '/:id/yritykset': wrap(Yritykset, details => {
      const id = idFromDetails(details);

      breadcrumbStore.set([
        { label: 'Yritykset', url: `#${prefix}/${id}/yritykset` }
      ]);
      return true;
    }),
    '/:id': wrap(ExistingLaatija, details => {
      const id = idFromDetails(details);

      breadcrumbStore.set([{ label: 'Yritykset', url: `#${prefix}/${id}` }]);
      return true;
    })
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
