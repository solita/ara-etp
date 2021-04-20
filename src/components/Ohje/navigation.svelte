<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as api from './ohje-api';

  import { location } from 'svelte-spa-router';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';
  import NavLink from '@Component/ohje/nav-link';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  let overlay = true;

  $: id = R.last(R.split('/ohje/', $location));
  let resources = Maybe.None();
  let whoami = Maybe.None();

  const hasNoParent = s => R.prop('parent-id', s).isNone();
  const hasParent = s => R.prop('parent-id', s).isSome();

  let sivutTree = [];

  const nestChildren = R.curry((data, obj) =>
    R.compose(
      R.assoc('children', R.__, obj),
      R.map(nestChildren(data)),
      R.filter(
        R.compose(Maybe.exists(R.equals(obj['id'])), R.prop('parent-id'))
      )
    )(data)
  );
  const toTree = data =>
    R.compose(R.map(nestChildren(data)), R.filter(hasNoParent))(data);

  const load = () => {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
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
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <nav class="w-full flex flex-col">
      {#each sivutTree as sivu}
        <NavLink {sivu} activeSivuId={id} />
      {/each}
      {#if Kayttajat.isPaakayttaja(whoami)}
        <div class="flex flex-col space-y-2 mt-4 justify-start font-semibold">
          <TextButton
            on:click={() => {
              alert('Order Links Button');
            }}
            icon="swap_vert"
            text={'Jarjestä_sivut'} />

          <Link
            href="/#/ohje/new"
            icon={Maybe.Some('add_circle_outline')}
            text={'Uusi_sivu'} />
        </div>
      {/if}
    </nav>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
