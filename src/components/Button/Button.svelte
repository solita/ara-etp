<script>
  export let text = '';
  export let type = 'primary';
</script>

<style>
  .button {
    @apply flex justify-center items-center px-6 py-2 rounded-full font-eb uppercase text-light tracking-widest;
  }

  .primary {
    @apply bg-primary;
  }

  .secondary {
    @apply bg-secondary;
  }

  .error {
    @apply bg-error;
  }
</style>

<button
  class="button"
  class:primary={type === 'primary'}
  class:secondary={type === 'secondary'}
  class:error={type === 'error'}
  on:click>
  {text}
</button>
