<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { currentUserStore } from '@/stores';

  import * as Navigation from '@Utility/navigation';
  import { _ } from '@Language/i18n';

  import Link from '@Component/Link/Link';

  let showDropdown = false;

  let namebutton;
  let nameNode;

  $: fullName = R.compose(
    Maybe.orSome('...'),
    R.map(kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`)
  )($currentUserStore);

  $: console.log(
    R.compose(
      R.concat(R.__, Navigation.defaultHeaderMenuLinks($_)),
      Maybe.orSome([]),
      R.map(Navigation.roleBasedHeaderMenuLinks($_))
    )($currentUserStore)
  );
</script>

<style type="text/postcss">
  header {
    @apply flex px-20 h-20 uppercase text-light justify-between items-center font-bold tracking-xl;
  }

  a {
    @apply px-4 py-2 text-dark text-center font-normal normal-case w-full tracking-normal cursor-pointer;
  }

  a:not(:last-child) {
    @apply border-b-1 border-disabled;
  }

  a:hover {
    @apply bg-background;
  }
</style>

<svelte:window
  on:click={event => {
    const itemNodes = nameNode.querySelectorAll('.dropdownitem');
    if (!R.includes(event.target, itemNodes) && event.target !== namebutton) {
      showDropdown = false;
    }
  }} />

<header>
  <img src="images/ara_logo_simple_nega.svg" alt="Ara" />
  <div>Tähän haku</div>
  <div>Laatijan ohjeet</div>
  <div>Työkalut</div>
  <div bind:this={nameNode} class="relative">
    <span
      bind:this={namebutton}
      class="cursor-pointer"
      on:click={() => (showDropdown = !showDropdown)}>
      {fullName}
    </span>
    <span class="material-icons absolute">keyboard_arrow_down</span>
    {#if showDropdown}
      <div class="absolute mt-2 w-48 bg-light shadow-xl flex flex-col">
        {#each R.compose( R.concat(R.__, Navigation.defaultHeaderMenuLinks($_)), Maybe.orSome([]), R.map(Navigation.roleBasedHeaderMenuLinks($_)) )($currentUserStore) as link}
          <a class="w-full" href={link.href}>{link.text}</a>
        {/each}
      </div>
    {/if}
  </div>
</header>
