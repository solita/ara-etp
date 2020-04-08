<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let items = [];
  export let active = Maybe.None();
  export let component = null;
  export let onclick;
</script>

<style type="text/postcss">
  ol {
    @apply absolute top-auto left-0 right-0 overflow-auto z-10;
    box-shadow: 0 25px 50px 0px rgba(0, 0, 0, 0.25); /* TODO: Generate in tailwind */
    max-height: 20em;
  }

  ol::-webkit-scrollbar {
    @apply w-2;
  }

  ol::-webkit-scrollbar-track {
    @apply bg-background;
  }

  ol::-webkit-scrollbar-thumb {
    @apply bg-disabled;
  }

  ol::-webkit-scrollbar-thumb:hover {
    @apply bg-dark;
  }

  li {
    @apply py-4 pl-6 cursor-pointer bg-light;
  }

  li:not(:first-child) {
    @apply border-disabled border-t-1;
  }

  li:hover {
    @apply bg-background text-dark;
  }

  .active {
    @apply outline-none bg-primary text-light;
  }
</style>

<ol transition:slide={{ duration: 200 }}>
  {#each items as item, index}
    <li
      class="dropdownitem"
      class:active={Maybe.fold(false, R.equals(index), active)}
      on:click={_ => onclick(item, index)}>
      {#if component}
        <svelte:component this={component} state={item} />
      {:else}{item}{/if}
    </li>
  {/each}
</ol>
