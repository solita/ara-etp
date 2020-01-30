<script>
  import { fade } from 'svelte/transition';

  export let type = 'warn';
  export let text = 'Failure';
  export let close = () => {};

  const warn = type === 'warn';
  const error = type === 'error';
  const success = type === 'success';
</script>

<style type="text/postcss">
  .alert {
    @apply flex justify-between py-2 bg-primary text-light rounded-lg;
  }

  .warn {
    @apply bg-orange-600;
  }

  .error {
    @apply bg-red-700;
  }

  .success {
    @apply bg-green-500;
  }

  .first-half {
    @apply ml-4;
  }

  .second-half {
    @apply mr-4;
  }

  .hidden {
    display: none;
  }
</style>

<div
  transition:fade={{ duration: 100 }}
  class:warn
  class:error
  class:success
  class="alert">
  <div class="first-half">
    <span>Y</span>
    <span>{text}</span>
  </div>
  <div class="second-half cursor-pointer">
    <span on:click={close}>X</span>
  </div>
</div>
