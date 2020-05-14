<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { currentUserStore } from '@/stores';

  let showDropdown = false;

  let namebutton;
  let nameNode;

  $: fullName = R.compose(
    Maybe.orSome('...'),
    R.map(kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`)
  )($currentUserStore);
</script>

<style type="text/postcss">
  header {
    @apply flex px-20 h-20 bg-secondary uppercase text-light justify-between items-center font-bold tracking-xl;
  }

  a {
    @apply px-4 py-2 text-dark text-center font-normal normal-case w-full tracking-normal;
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
      <div class="absolute mt-2 py-2 w-48 bg-light shadow-xl">
        <a href="/api/logout">Kirjaudu ulos</a>
      </div>
    {/if}
  </div>
</header>
