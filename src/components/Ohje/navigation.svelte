<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';
  import NavLink from '@Component/ohje/nav-link';
  import { arrayToObject } from 'qs/lib/utils';

  export let sivut = [];
  export let activeSivuId;
  export let whoami;

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

  $: console.log('sivut', sivut);
</script>

<nav class="w-full flex flex-col max-w-xs">
  {#each sivut as sivu}
    <NavLink {sivu} {activeSivuId} />
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
</nav>
