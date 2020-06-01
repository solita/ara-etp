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
    currentUserStore
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
    '/all': Energiatodistukset,
    '/:version/new': NewEnergiatodistus,
    '/:version/:id': ExistingEnergiatodistus,
    '/:version/:id/liitteet': Liitteet,
    '/:version/:id/allekirjoitus': Allekirjoitus
  };
</script>

<svelte:window
  on:hashchange={_ => flashMessageStore.flush('Energiatodistus')} />

<Router {routes} {prefix} />
<div class="w-full min-h-3em sticky bottom-0">
  <FlashMessage module={'Energiatodistus'} />
</div>
