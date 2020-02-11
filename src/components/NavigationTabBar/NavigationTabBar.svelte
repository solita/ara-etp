<script>
  import NavigationTab from '../NavigationTab/NavigationTab.svelte';

  export let links;
</script>

<style>
  .navigationtabbar {
    @apply flex pb-4;
  }

  .navigationtabbar:last-child::after {
    content: '';
    @apply flex-grow border-dark border-b-3;
  }
</style>

<div class="navigationtabbar">
  {#each links as link}
    <NavigationTab text={link.text} />
  {/each}
</div>
