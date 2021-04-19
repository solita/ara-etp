<script>
  import Router from 'svelte-spa-router';

  import OhjeViewer from './viewer';
  import OhjeEditor from './editor';
  import OhjeCreator from './creator';
  import Navigation from './navigation';
  import NotFound from '@Component/NotFound/NotFound';

  import FlashMessage from '@Component/FlashMessage/FlashMessage';
  import { flashMessageStore } from '@/stores';

  const prefix = '/ohje';
  const routes = {
    '/new': OhjeCreator,
    '/:id/edit': OhjeEditor,
    '/:id': OhjeViewer,
    '*': NotFound
  };
</script>

<svelte:window on:hashchange={_ => flashMessageStore.flush('ohje')} />
<div class="flex space-x-2">
  <div class="w-2/6">
    <Navigation />
  </div>
  <div class="w-4/6">
    <Router {routes} {prefix} />
  </div>
</div>

<FlashMessage module={'ohje'} />

<!-- <script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as api from './ohje-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Navigation from '@Component/Ohje/navigation.svelte';
  import OhjeViewer from '@Component/Ohje/viewer.svelte';

  export let params;

  let overlay = true;
  let resources = Maybe.None();
  $: id = params.id;

  // $: {
  //   overlay = true;
  //   Future.fork(
  //     response => {
  //       const msg = $_(
  //         Maybe.orSome(
  //           'viesti.all.messages.load-error',
  //           Response.localizationKey(response)
  //         )
  //       );

  //       flashMessageStore.add('viesti', 'error', msg);
  //       overlay = false;
  //     },
  //     response => {
  //       resources = Maybe.Some(response);
  //       overlay = false;
  //     },
  //     Future.parallelObject(3, {
  //       whoami: kayttajaApi.whoami,
  //       sivu: api.getSivu(id),
  //       sivut: api.getSivut
  //     })
  //   );
  // }
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex space-x-3">
    {#each Maybe.toArray(resources) as { sivut, sivu, whoami }}
      <div class="w-2/6">
        <Navigation activeSivuId={id} {sivut} {whoami} />
      </div>
      <div class="w-4/6">
        <OhjeViewer {sivu} {whoami} />
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay> -->
