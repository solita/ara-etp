<script>
  export let text = '';
  export let icon = '';
  export let type = 'submit';
  export let title = '';
  export let disabled = false;
</script>

<style type="text/postcss">
  button {
    @apply flex items-center justify-start border-b-1 border-transparent font-semibold text-primary;
  }

  button:hover:not(:disabled) {
    @apply border-primary;
  }

  button:active {
    @apply text-secondary;
  }

  button:focus {
    @apply outline-none;
  }

  button:disabled {
    @apply cursor-not-allowed text-disabled;
  }
</style>

<button {type} {title} {disabled} on:click>
  <span class="mr-1 font-icon">{icon}</span>
  {text}
</button>
