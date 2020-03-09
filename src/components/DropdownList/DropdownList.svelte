<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let items = [];
  export let active = Maybe.None();
  export let component = null;
  export let onclick;
</script>

<style type="text/postcss">
  ol {
    @apply absolute top-auto left-0 right-0 shadow-lg;
  }

  li {
    @apply py-2 pl-6 cursor-pointer bg-light;
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

<ol>
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
