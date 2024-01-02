<script>
  export let text = '';
  export let icon = '';
  export let type = 'submit';
  export let title = '';
  export let disabled = false;
</script>

<style type="text/postcss">
  button {
    @apply flex justify-start items-center text-primary border-b-1 border-transparent font-semibold;
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
    @apply text-disabled cursor-not-allowed;
  }
</style>

<button {type} {title} {disabled} on:click>
  <span class="font-icon mr-1">{icon}</span>
  {text}
</button>
