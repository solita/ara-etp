<script>
  import * as R from 'ramda';
  import Router, { location, wrap } from 'svelte-spa-router';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Navigation from '@Utility/navigation';

  import { _ } from '@Language/i18n';
  import ExistingEnergiatodistus from './ExistingEnergiatodistus';
  import NewEnergiatodistus from './NewEnergiatodistus';
  import Energiatodistukset from './Energiatodistukset';
  import Liitteet from './Liitteet';
  import Allekirjoitus from './Allekirjoitus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import {
    flashMessageStore,
    navigationStore,
    currentUserStore,
    breadcrumbStore
  } from '@/stores';
  import * as et from './energiatodistus-utils';

  const idAndVersionFromLocation = R.compose(
    Maybe.fromEmpty,
    R.unless(
      R.allPass([
        R.compose(
          R.equals(2),
          R.length
        ),
        R.complement(R.includes)('new')
      ]),
      R.always([])
    ),
    R.slice(1, 3),
    R.tail,
    R.split('/')
  );

  const prefix = '/energiatodistus';
  const routes = {
    '/all': wrap(Energiatodistukset, _ => {
      breadcrumbStore.set([et.breadcrumb1stLevel($_)]);

      return true;
    }),
    '/:version/new': wrap(NewEnergiatodistus, details => {
      const version = R.compose(
        R.nth(1),
        R.tail,
        R.split('/'),
        R.prop('location')
      )(details);

      breadcrumbStore.set([
        et.breadcrumb1stLevel($_),
        {
          label: `Uusi energiatodistus ${version}`,
          url: location.href
        }
      ]);

      return true;
    }),
    '/:version/:id': wrap(ExistingEnergiatodistus, details => {
      const [version, id] = R.compose(
        Maybe.get,
        idAndVersionFromLocation,
        R.prop('location')
      )(details);

      breadcrumbStore.set([
        et.breadcrumb1stLevel($_),
        {
          label: `ET ${id}`,
          url: location.href
        }
      ]);

      return true;
    }),
    '/:version/:id/liitteet': wrap(Liitteet, details => {
      const [version, id] = R.compose(
        Maybe.get,
        idAndVersionFromLocation,
        R.prop('location')
      )(details);

      breadcrumbStore.set([
        et.breadcrumb1stLevel($_),
        {
          label: `ET ${id}`,
          url: location.href
        }
      ]);

      return true;
    }),
    '/:version/:id/sign': wrap(Allekirjoitus, details => {
      const [version, id] = R.compose(
        Maybe.get,
        idAndVersionFromLocation,
        R.prop('location')
      )(details);

      breadcrumbStore.set([
        et.breadcrumb1stLevel($_),
        {
          label: `ET ${id}`,
          url: location.href
        }
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
    R.map(Navigation.linksForLaatija, $currentUserStore),
    R.compose(
      R.map(R.apply(Navigation.linksForEnergiatodistus)),
      idAndVersionFromLocation
    )($location)
  ]);
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em">
  <FlashMessage module={'Energiatodistus'} />
</div>
