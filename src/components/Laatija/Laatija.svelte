<script>
  import * as R from 'ramda';
  import Router, { wrap } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Navigation from '@Utility/navigation';

  import LaatijaUpload from '@Component/LaatijaUpload/LaatijaUpload';
  import Yritykset from '@Component/Laatija/Yritykset';
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
    breadcrumbStore,
    currentUserStore
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
    })
  };

  $: R.compose(
    navigationStore.set,
    Maybe.get,
    R.last,
    R.filter(Maybe.isSome)
  )([
    Maybe.of([{ text: '...', href: '' }]),
    R.map(Navigation.linksForKayttaja, $currentUserStore)
  ]);
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Laatija')} />

<Country />
<ToimintaAlueet />
<Patevyydet />

{#if $countryStore.isRight() && $toimintaAlueetStore.isRight() && $patevyydetStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Laatija'} />
