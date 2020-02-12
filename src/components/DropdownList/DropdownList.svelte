<script>
  import { slide } from 'svelte/transition';
  import { onMount } from 'svelte';

  export let state = [];
  export let component;

  export let passFocusableNodesToParent = () => {};

  let node;

  onMount(() =>
    passFocusableNodesToParent(node.querySelectorAll('.dropdownitem'))
  );
</script>

<style>
  .dropdownlist {
    @apply absolute top-auto left-0 right-0 shadow-lg;
  }

  .dropdownitem {
    @apply py-2 pl-6 cursor-pointer bg-light;
  }

  .dropdownitem:not(:first-child) {
    @apply border-disabled border-t-1;
  }

  .dropdownitem:hover {
    @apply bg-background text-dark;
  }

  .dropdownitem:focus {
    @apply outline-none bg-hover text-light;
  }
</style>

<ol transition:slide class="dropdownlist" bind:this={node}>
  {#each state as item}
    <li class="dropdownitem" tabindex="0" on:click on:blur>
      {#if component}
        <svelte:component this={component} state={item} />
      {:else}{item}{/if}
    </li>
  {/each}
</ol>
