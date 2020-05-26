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
    currentUserStore,
    navigationStore
  } from '@/stores';

  const prefix = '/yritys';
  const routes = {
    '/new': NewYritys,
    '/all': Yritykset,
    '/:id': ExistingYritys
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
