<script>
  import { fade } from 'svelte/transition';

  export let overlay = false;
</script>

<style type="text/postcss">
  .overlay-container {
    @apply flex w-full;
  }

  .overlayed-content {
    @apply w-full;
  }

  .overlay {
    @apply absolute bottom-0 left-0 right-0 top-0 flex items-center justify-center bg-light opacity-75;
  }
</style>

<div class="overlay-container">
  <div class="overlayed-content">
    <slot name="content" />
  </div>
  {#if overlay}
    <div transition:fade={{ duration: 50 }} class="overlay">
      <slot name="overlay-content" />
    </div>
  {/if}
</div>
