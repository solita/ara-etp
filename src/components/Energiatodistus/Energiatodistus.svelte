<script>
  import Router, { wrap } from 'svelte-spa-router';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';
  import ExistingEnergiatodistus from './ExistingEnergiatodistus';
  import NewEnergiatodistus from './NewEnergiatodistus';
  import Energiatodistukset from './Energiatodistukset';
  import Liitteet from './Liitteet';
  import Allekirjoitus from './Allekirjoitus';
  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore, breadcrumbStore } from '@/stores';
  import * as et from './energiatodistus-utils';

  const idAndVersionFromDetails = R.compose(
    R.slice(1, 3),
    R.tail,
    R.split('/'),
    R.prop('location')
  );

  const prefix = '/energiatodistus';
  const routes = {
    '/all': wrap(Energiatodistukset, _ => {
      breadcrumbStore.set([et.breadcrumb1stLevel($_)]);

      return true;
    }),
    '/:version/new': wrap(NewEnergiatodistus, details => {
      const [version, id] = idAndVersionFromDetails(details);

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
      const [version, id] = idAndVersionFromDetails(details);

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
      const [version, id] = idAndVersionFromDetails(details);

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
      const [version, id] = idAndVersionFromDetails(details);

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
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em">
  <FlashMessage module={'Energiatodistus'} />
</div>
