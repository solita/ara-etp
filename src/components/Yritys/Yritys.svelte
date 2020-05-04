<script>
  import * as R from 'ramda';
  import Router, { wrap } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Navigation from '@Utility/navigation';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import Yritykset from './Yritykset';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import Country from '@Component/Geo/Country';
  import {
    flashMessageStore,
    countryStore,
    breadcrumbStore,
    currentUserStore,
    navigationStore
  } from '@/stores';

  const idFromDetails = R.compose(
    R.head,
    R.tail,
    R.split('/'),
    R.prop('location')
  );

  const prefix = '/yritys';
  const routes = {
    '/new': wrap(NewYritys, _ => {
      breadcrumbStore.set([{ label: 'Uusi yritys', url: `#${prefix}/new` }]);
      return true;
    }),
    '/all': wrap(Yritykset, _ => {
      breadcrumbStore.set([{ label: 'Yritykset', url: `#${prefix}/all` }]);
      return true;
    }),
    '/:id': wrap(ExistingYritys, details => {
      const id = idFromDetails(details);
      breadcrumbStore.set([{ label: `Yritys ${id}`, url: `#${prefix}/${id}` }]);
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

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<Country />

{#if $countryStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Yritys'} />
