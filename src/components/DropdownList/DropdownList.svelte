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

  li:focus {
    @apply outline-none bg-hover text-light;
  }
</style>

<ol transition:slide bind:this={node}>
  {#each state as item}
    <li tabindex="0" on:click on:blur>
      {#if component}
        <svelte:component this={component} state={item} />
      {:else}{item}{/if}
    </li>
  {/each}
</ol>
