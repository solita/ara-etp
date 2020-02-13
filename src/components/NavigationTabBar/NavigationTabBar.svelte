<script>
  import NavigationTab from '../NavigationTab/NavigationTab.svelte';

  export let links;
</script>

<style type="text/postcss">
  div {
    @apply flex pb-4;
  }

  div:last-child::after {
    content: '';
    @apply flex-grow border-dark border-b-3;
  }
</style>

<div>
  {#each links as link}
    <NavigationTab text={link.text} />
  {/each}
</div>
