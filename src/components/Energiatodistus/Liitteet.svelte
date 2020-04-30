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
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';

  export let params;

  const emptyLiite = _ => ({ nimi: '', url: ''});

  let overlay = true;
  let failure = false;
  let liitteet = [];
  let liiteLinkAdd = emptyLiite();

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');
  const cancel = _ => { liiteLinkAdd = emptyLiite(); }

  const load = R.compose(
      Future.fork(
          R.compose(
              R.tap(toggleOverlay(false)),
              R.tap(flashMessageStore.add('Energiatodistus', 'error')),
              R.partial($_, ['energiatodistus.liitteet.messages.load-error'])),
          R.compose(
              R.tap(toggleOverlay(false)),
              response => {
                liitteet = response;
              }
          )),
      R.tap(toggleOverlay(true)),
      api.getLiitteetById(fetch)
  );
  const fork = Future.fork(
    R.compose(
        R.tap(toggleOverlay(false)),
        R.tap(flashMessageStore.add('Energiatodistus', 'error')),
        R.partial($_, ['energiatodistus.liitteet.messages.save-error'])),
    R.compose(
        R.tap(flashMessageStore.add('Energiatodistus', 'success')),
        R.partial($_, ['energiatodistus.liitteet.messages.save-success']),
        R.partial(load, [params.version, params.id])));

  const uploadFiles = R.compose(
    fork,
    R.tap(toggleOverlay(true)),
    api.postLiitteetFiles(fetch, params.version, params.id)
  );

  const addLink = R.compose(
    fork,
    R.tap(toggleOverlay(true)),
    api.postLiitteetLink(fetch, params.version, params.id)
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
              <td>{liite.createtime}</td>
              <td><Link text={liite.nimi} href={liite.url} /></td>
              <td>{liite['author-fullname']}</td>
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
      <FileDropArea onchange={uploadFiles} multiple={true}/>
    </div>
    <div class="w-1/2 flex flex-col">
      <H2 text={'Lisää linkki'} />
      <form on:submit|preventDefault={_ => addLink(liiteLinkAdd)}>
        <div class="w-full px-4 py-4">
          <Input
              label={'Nimi'}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['nimi'])} />
        </div>

        <div class="w-full px-4 py-4">
          <Input
              label={'URL'}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['url'])} />
        </div>

        <div class="flex -mx-4 pt-8">
          <div class="px-4">
            <Button type={'submit'} text={'Lisää linkki'} />
          </div>
          <div class="px-4">
            <Button on:click = { cancel } text={'Tyhjennä'}
                    type={'reset'}
                    style={'secondary'} />
          </div>
        </div>

      </form>
    </div>
  </div>
</div>