<script>
  let showDropdown = false;
</script>

<style type="text/postcss">
  header {
    @apply flex px-20 h-20 bg-secondary uppercase text-light justify-between items-center font-extrabold;
  }
</style>

<header>
  <img src="images/ara_logo_simple_nega.svg" alt="Ara" />
  <div>Tähän haku</div>
  <div>Laatijan ohjeet</div>
  <div>Työkalut</div>
  <div class="relative">
    <span
      class="cursor-pointer"
      on:click={() => (showDropdown = !showDropdown)}>
      Laila Laatija
    </span>
    <span class="material-icons absolute">keyboard_arrow_down</span>
    {#if showDropdown}
      <div class="absolute mt-2 py-2 w-48 bg-light shadow-xl">
        <a
          class="px-4 py-2 text-dark text-center font-light normal-case w-full"
          href="/api/logout">
          Kirjaudu ulos
        </a>
      </div>
    {/if}
  </div>
</header>
