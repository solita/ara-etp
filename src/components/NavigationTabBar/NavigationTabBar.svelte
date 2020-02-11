<script>
  import NavigationTab from '../NavigationTab/NavigationTab.svelte';

  export let links;
</script>

<style>
  .navigationtabbar {
    @apply flex;
  }
</style>

<div class="navigationtabbar">
  {#each links as link}
    <NavigationTab text={link.text} />
  {/each}
</div>
