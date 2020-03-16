<script>
  export let text = '';
  export let type = 'button';
  export let style = 'primary';
  export let disabled = false;
</script>

<style type="text/postcss">
  button {
    @apply flex justify-center items-center px-6 py-2 rounded-full font-extrabold uppercase text-light tracking-widest;
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

  .disabled {
    @apply bg-disabled cursor-not-allowed;
  }
</style>

<button
  {type}
  class:primary={style === 'primary'}
  class:secondary={style === 'secondary'}
  class:error={style === 'error'}
  class:disabled
  {disabled}
  on:click>
  {text}
</button>
