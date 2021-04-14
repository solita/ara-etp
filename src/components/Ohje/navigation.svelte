<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';
  import TextButton from '@Component/Button/TextButton';
  import Link from '@Component/Link/Link';
  import NavLink from '@Component/ohje/nav-link';

  export let sivut = [];
  export let activeSivuId;
  export let whoami;

  const hasNoParent = s => R.prop('parent-id', s).isNone();
  const hasParent = s => R.prop('parent-id', s).isSome();

  let navi = R.compose(
    R.map(
      R.assoc(
        'children',
        R.find(R.propEq('parent-id', R.__.id))(R.filter(hasParent, sivut))
      )
    ),
    R.filter(hasNoParent)
  )(sivut);

  $: console.log('navi', navi);
  $: console.log('find', R.find(R.propEq('parent-id', Maybe.Some(5)))(sivut));
  // for each
  // if has parent id, add to that objects child array
  // remove original
  // order by ordinal in child arrays and in root

  // $: console.log('parent', R.prop('parent-id'));

  // let test = R.filter(hasParent, sivut);
  // $: console.log('test', test);
</script>

<style>
  .root {
    @apply bg-secondary text-light border-light;
  }
  .child {
    @apply bg-light text-dark border-secondary;
  }
  .active .title {
    @apply font-bold underline;
  }
</style>

<nav class="w-full flex flex-col max-w-xs">
  {#each sivut as sivu}
    <NavLink {sivu} {activeSivuId} } />
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
