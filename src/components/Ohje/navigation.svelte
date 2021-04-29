<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';

  import { flashMessageStore } from '@/stores';
  import { location } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';
  import NavLink from '@Component/Ohje/nav-link';
  // import Overlay from '@Component/Overlay/Overlay.svelte';
  // import Spinner from '@Component/Spinner/Spinner.svelte';
  // let overlay = true;

  $: id = R.last(R.split('/ohje/', $location));
  let whoami = Maybe.None();
  let sivutTree = [];
  let sortMode = false;

  const hasParent = s => R.prop('parent-id', s).isSome();
  const hasNoParent = s => R.prop('parent-id', s).isNone();
  const sortByOrdinalTitle = R.sortWith([
    R.ascend(R.prop('ordinal')),
    R.ascend(R.compose(R.toLower, R.prop('title')))
  ]);

  const nestChildren = R.curry((data, obj) =>
    R.compose(
      R.assoc('children', R.__, obj),
      sortByOrdinalTitle,
      R.map(nestChildren(data)),
      R.filter(
        R.compose(Maybe.exists(R.equals(obj['id'])), R.prop('parent-id'))
      )
    )(data)
  );

  const toTree = data =>
    R.compose(
      R.map(nestChildren(data)),
      sortByOrdinalTitle,
      R.filter(hasNoParent)
    )(data);

  const load = () => {
    // overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        // overlay = false;
      },
      response => {
        sivutTree = toTree(response.sivut);
        whoami = Maybe.Some(response.whoami);
        // overlay = false;
      },
      Future.parallelObject(2, {
        whoami: kayttajaApi.whoami,
        sivut: api.getSivut
      })
    );
  };

  load();

  const updateSivu = (id, body) =>
    R.compose(
      Future.fork(
        response => {
          const msg = $_(
            Maybe.orSome(
              'ohje.navigation.sort-success',
              Response.localizationKey(response)
            )
          );
          flashMessageStore.add('ohje', 'error', msg);
          // overlay = false;
        },
        _ => {
          flashMessageStore.add(
            'ohje',
            'success',
            $_('ohje.navigation.sort-success')
          );
          // overlay = false;
          load();
        }
      ),
      // R.tap(enableOverlay),
      api.putSivu(fetch, id)
    )(body);

  const toggleSortMode = () => {
    sortMode = !sortMode;
  };
</script>

<nav class="w-full flex flex-col">
  {#each sivutTree as sivu}
    <NavLink {sivu} activeSivuId={id} draggable={sortMode} {updateSivu} />
  {/each}
  {#each Maybe.toArray(whoami) as whoami}
    {#if Kayttajat.isPaakayttaja(whoami)}
      <div class="flex flex-col space-y-2 mt-4 justify-start font-semibold">
        {#if !R.isEmpty(sivutTree)}
          {#if !sortMode}
            <TextButton
              on:click={toggleSortMode}
              icon="swap_vert"
              text={$_('ohje.navigation.sort')} />
          {:else}
            <TextButton
              on:click={toggleSortMode}
              icon="highlight_off"
              text={$_('ohje.navigation.end-sort')} />
          {/if}
        {/if}
        {#if id !== 'new'}
          <Link
            href="/#/ohje/new"
            disabled={sortMode}
            icon={Maybe.Some('add_circle_outline')}
            text={$_('ohje.navigation.create')} />
        {/if}
      </div>
    {/if}
  {/each}
</nav>
