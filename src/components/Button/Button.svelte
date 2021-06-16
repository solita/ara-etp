<script>
  export let text = '';
  export let type = 'button';
  export let style = 'primary';
  export let disabled = false;
  export let prefix = '';
  export let title = '';
</script>

<style type="text/postcss">
  button {
    @apply flex justify-center items-center rounded-full font-bold uppercase text-light tracking-xl min-w-10;
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

<!-- purgecss: primary secondary error disabled pr-12 -->
<button
  class="relative px-6 py-3"
  {type}
  data-cy={`${prefix}-${type}`}
  class:primary={style === 'primary'}
  class:secondary={style === 'secondary'}
  class:error={style === 'error'}
  class:disabled
  class:pr-12={$$slots.default}
  {disabled}
  {title}
  on:click|stopPropagation>
  {text}
  <div class="absolute top-0 right-0">
    <slot />
  </div>
</button>
