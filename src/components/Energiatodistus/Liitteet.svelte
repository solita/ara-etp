<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as validation from '@Utility/validation';
  import * as api from './energiatodistus-api';
  import * as et from './energiatodistus-utils';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, breadcrumbStore } from '@/stores';

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

  const liiteLinkAddSchema = {
    nimi: [validation.isRequired,
           validation.maxLengthConstraint(300)],
    url: [validation.isRequired, validation.urlValidator]
  }

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

  const submit = event => {
    if (et.isValidForm(liiteLinkAddSchema, liiteLinkAdd)) {
      flashMessageStore.flush();
      addLink(liiteLinkAdd);
    } else {
      flashMessageStore.add('Energiatodistus', 'error',
          $_('energiatodistus.messages.validation-error'));
    }
  }

  $: load(params.version, params.id);

  const liiteUrl = liite => Maybe.orSome(
      api.url.liitteet(params.version, params.id) + '/' + liite.id + '/content',
      liite.url);

  const addDefaultProtocol =
      R.ifElse(R.includes('://'), R.identity, R.concat('http://'))

  const deleteLiite = R.compose(
      fork,
      R.tap(toggleOverlay(true)),
      api.deleteLiite(fetch, params.version, params.id)
  );
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
              <td><Link text={liite.nimi} href={liiteUrl(liite)} /></td>
              <td>{liite['author-fullname']}</td>
              <td on:click={_ => deleteLiite(liite.id)} class="cursor-pointer">
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
    <div class="lg:w-1/2 w-full mr-6 mb-6">
      <H2 text={'Lisää tiedosto'} />
      <FileDropArea onchange={uploadFiles} multiple={true}/>
    </div>
    <div class="lg:w-1/2 w-full flex flex-col">
      <H2 text={'Lisää linkki'} />
      <form on:submit|preventDefault={submit}>
        <div class="w-full px-4 py-4">
          <Input
              id={'link.nimi'}
              name={'link.nimi'}
              label={'Nimi'}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['nimi'])}
              parse={R.trim}
              validators={liiteLinkAddSchema.nimi}
              i18n={$_}/>
        </div>

        <div class="w-full px-4 py-4">
          <Input
              id={'link.url'}
              name={'link.url'}
              label={'URL'}
              bind:model={liiteLinkAdd}
              lens={R.lensPath(['url'])}
              parse={R.compose(addDefaultProtocol, R.trim)}
              validators={liiteLinkAddSchema.url}
              i18n={$_}/>
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