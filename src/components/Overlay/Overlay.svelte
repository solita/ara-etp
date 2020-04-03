<script>
  import { fade } from 'svelte/transition';

  export let overlay = false;
</script>

<style type="text/postcss">
  .overlay-container {
    @apply w-full flex;
  }

  .overlayed-content {
    @apply flex-grow;
  }

  .overlay {
    @apply absolute bg-light opacity-75 top-0 bottom-0 left-0 right-0 flex justify-center items-center;
  }

  .overlayed-content > * {
    @apply flex-grow;
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
