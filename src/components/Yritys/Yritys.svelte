<script>
  import * as R from 'ramda';
  import Router, { wrap } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import NewYritys from './NewYritys';
  import ExistingYritys from './ExistingYritys';
  import Yritykset from './Yritykset';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import Country from '@Component/Geo/Country';
  import { flashMessageStore, countryStore, breadcrumbStore } from '@/stores';

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
</script>

<style type="text/postcss">

</style>

<svelte:window on:hashchange={_ => flashMessageStore.flush('Yritys')} />

<Country />

{#if $countryStore.isRight()}
  <Router {routes} {prefix} />
{/if}

<FlashMessage module={'Yritys'} />
