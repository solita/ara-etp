<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';
  import NavLink from '@Component/ohje/nav-link';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  export let id;

  const i18n = $_;
  let whoami = Maybe.None();
  let sivutTree = [];
  let sortMode = false;
  let overlay = true;

  const enableOverlay = _ => {
    overlay = true;
  };

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
      R.filter(R.propSatisfies(Maybe.isNone, 'parent-id'))
    )(data);

  const load = () => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        sivutTree = toTree(response.sivut);
        whoami = Maybe.Some(response.whoami);
        overlay = false;
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
          const msg = i18n(
            Maybe.orSome(
              'ohje.navigation.sort-success',
              Response.localizationKey(response)
            )
          );
          flashMessageStore.add('ohje', 'error', msg);
          overlay = false;
        },
        _ => {
          flashMessageStore.add(
            'ohje',
            'success',
            i18n('ohje.navigation.sort-success')
          );
          overlay = false;
          load();
        }
      ),
      R.tap(enableOverlay),
      api.putSivu(fetch, id)
    )(body);

  const toggleSortMode = () => {
    sortMode = !sortMode;
  };
</script>

<Overlay {overlay}>
  <nav slot="content" class="w-full flex flex-col">
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
                text={i18n('ohje.navigation.sort')} />
            {:else}
              <TextButton
                on:click={toggleSortMode}
                icon="highlight_off"
                text={i18n('ohje.navigation.end-sort')} />
            {/if}
          {/if}
          <Link
            href="/#/ohje/new"
            disabled={sortMode}
            icon={Maybe.Some('add_circle_outline')}
            text={i18n('ohje.navigation.create')} />
        </div>
      {/if}
    {/each}
  </nav>

  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
