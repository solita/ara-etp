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

  const hasNoParent = s => R.prop('parent-id', s).isNone();
  const hasParent = s => R.prop('parent-id', s).isSome();

  // const unflatten = (array, parent, tree) => {
  //   tree = typeof tree !== 'undefined' ? tree : [];
  //   parent =
  //     typeof parent !== 'undefined'
  //       ? R.evolve({
  //           id: Maybe.fromNull
  //         })(parent)
  //       : { id: Maybe.None() };

  //   var children = array.filter(function(child) {
  //     return R.equals(child['parent-id'], parent.id);
  //   });

  //   if (children.length) {
  //     if (Maybe.isNone(parent.id)) {
  //       tree = children;
  //     } else {
  //       parent = R.mergeWith(R.concat, { children: children }, parent);
  //       console.log('parent', parent);
  //     }
  //     children.forEach(function(child) {
  //       unflatten(array, child);
  //     });
  //   }

  //   return tree;
  // };

  // let navi = unflatten(sivut);
  // $: console.log('navi', navi);

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
        resources = Maybe.Some(response);
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
  <div slot="content" class="w-full mt-3 flex space-x-3">
    <nav class="w-full flex flex-col max-w-xs">
      {#each Maybe.toArray(resources) as { sivut, whoami }}
        {#each sivut as sivu}
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
          </div>{/if}
      {/each}
    </nav>
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
