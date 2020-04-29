<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as api from './energiatodistus-api';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, breadcrumbStore } from '@/stores';
  import { push } from '@Component/Router/router';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';
  import FileDropArea from '@Component/FileDropArea/FileDropArea';

  export let params;

  let overlay = true;
  let failure = false;
  let liitteet = [];

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');

  const load = R.compose(
      Future.fork(
          R.compose(
              R.tap(toggleOverlay(false)),
              R.tap(flashMessageStore.add('Energiatodistus', 'error')),
              R.partial($_, ['energiatodistus.liitteet.messages.load-error'])),
          R.compose(
              response => {
                liitteet = response[0];
              },
              R.tap(toggleOverlay(false))
          )),
      R.tap(toggleOverlay(true)),
      api.getLiitteetById(fetch)
  );

  const upload = R.compose(
    Future.fork(
      R.compose(
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Energiatodistus', 'error')),
        R.partial($_, ['energiatodistus.liitteet.messages.save-error'])),
      R.compose(
        R.tap(flashMessageStore.add('Energiatodistus', 'success')),
        R.partial($_, ['energiatodistus.liitteet.messages.save-success']),
        R.partial(load, [params.version, params.id]))),
    R.tap(toggleOverlay(true)),
    api.postLiitteetFiles(fetch, params.version, params.id)
  );

  $: load(params.version, params.id);
</script>

<style>
  table {
    @apply w-full;
  }

  th {
    @apply px-4 py-2 text-center;
  }

  tr {
    @apply px-4 py-2;
  }

  td {
    @apply text-center;
  }

  tr:nth-child(even) {
    @apply bg-background;
  }
</style>

<div class="w-full mt-3">
  <H1 text={'Liitteet'} />

  <Overlay {overlay}>
    <div slot="content" class="mb-10">
      {#if R.isEmpty(liitteet)}
        <p>Energiatodistuksella ei ole liitteitä.</p>
      {:else}
        <table>
          <thead>
            <tr>
              <th>Lisätty</th><th>Tiedosto / Linkki</th><th>Lisääjä</th><th>Toiminnot</th>
            </tr>
          </thead>
          <tbody>
          {#each liitteet as liite}
            <tr>
              <td>13.3.2030</td>
              <td>a.pdf</td>
              <td>Lasse</td>
              <td>
                <span class="material-icons">delete</span>
              </td>
            </tr>
          {/each}
          </tbody>
        </table>
      {/if}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>

  <div class="mb-4 flex lg:flex-row flex-col">
    <div class="w-1/2 mr-6 mb-6">
      <H2 text={'Lisää tiedosto'} />
      <FileDropArea onchange={upload} multiple={true}/>
    </div>
    <div class="w-1/2">
      <H2 text={'Lisää linkki'} />
    </div>
  </div>
</div>