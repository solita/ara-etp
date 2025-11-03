<script>
  import Spinner from '@Component/Spinner/Spinner';

  export let text = '';
  export let type = 'button';
  export let style = 'primary';
  export let disabled = false;
  export let prefix = '';
  export let title = '';
  export let showSpinner = false;
</script>

<style type="text/postcss">
  button {
    @apply relative flex min-w-10 items-center justify-center rounded-full px-6 py-3 font-bold uppercase tracking-xl text-light;
    transition:
      padding-right 0.5s,
      padding-left 0.5s;
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
    @apply cursor-not-allowed bg-disabled;
  }
  .spinner-showing {
    @apply pl-2 pr-10;
  }

  .spinner-container {
    transition: opacity 0.5s;
  }

  .disabled .spinner-container {
    @apply opacity-100;
  }
</style>

<button
  {type}
  data-cy={`${prefix}-${type}`}
  class:primary={style === 'primary'}
  class:secondary={style === 'secondary'}
  class:error={style === 'error'}
  class:disabled
  class:spinner-showing={showSpinner}
  {disabled}
  {title}
  on:click|stopPropagation>
  {text}
  {#if showSpinner}
    <div class="spinner-container absolute right-0 top-0 opacity-0">
      <Spinner smaller={true} />
    </div>
  {/if}
</button>
