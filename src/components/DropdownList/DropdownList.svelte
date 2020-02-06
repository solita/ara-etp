<script>
  import { slide } from 'svelte/transition';

  export let state = [];
  export let component;
</script>

<style>
  .dropdownlist {
    @apply shadow-lg;
  }

  .dropdownitem {
    @apply py-2 pl-6 cursor-pointer bg-light;
  }

  .dropdownitem:nth-child(odd) {
    @apply bg-tablebackground;
  }

  .dropdownitem:hover {
    @apply bg-disabled;
  }
</style>

<ul transition:slide class="dropdownlist">
  {#each state as item}
    <li class="dropdownitem">
      {#if component}
        <svelte:component this={component} state={item} />
      {:else}{item}{/if}
    </li>
  {/each}
</ul>
