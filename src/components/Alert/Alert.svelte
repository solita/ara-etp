<script>
  import { slide } from 'svelte/transition';

  export let type = 'success';
  export let text = 'Failure';
  export let close = () => {};

  const error = type === 'error';
  const warn = type === 'warn';
  const success = type === 'success';

  const icon = {
    error: 'error_outline',
    warn: 'warning',
    success: 'check_circle_outline'
  }[type];
</script>

<style type="text/postcss">
  .alert {
    @apply flex justify-around py-2 px-2 bg-primary text-light rounded-lg shadow-lg;
  }

  .error {
    @apply bg-error;
  }

  .success {
    @apply bg-success;
  }

  .alert-text {
    @apply flex-grow pl-2;
  }

  .icon,
  .close {
    @apply font-icon;
  }

  .close {
    @apply cursor-pointer;
  }
</style>

<div
  transition:slide={{ duration: 200 }}
  class:error
  class:success
  class="alert">
  <span class="icon">{icon}</span>
  <span class="alert-text">{text}</span>
  <span class="close" on:click={close}>close</span>
</div>
