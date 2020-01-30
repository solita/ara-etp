<script>
  import { fade } from 'svelte/transition';

  export let type = 'success';
  export let text = 'Failure';
  export let close = () => {};

  const error = type === 'error';
  const success = type === 'success';
</script>

<style type="text/postcss">
  .alert {
    @apply flex justify-between py-2 bg-primary text-light rounded-lg;
  }

  .error {
    @apply bg-error;
  }

  .success {
    @apply bg-success;
  }

  .first-half {
    @apply ml-4;
  }

  .second-half {
    @apply mr-4;
  }
</style>

<div
  transition:fade={{ duration: 100 }}
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
