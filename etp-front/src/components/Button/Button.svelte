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
    @apply relative px-6 py-3 flex justify-center items-center rounded-full font-bold uppercase text-light tracking-xl min-w-10;
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
    @apply bg-disabled cursor-not-allowed;
  }
  .spinner-showing {
    @apply pr-10 pl-2;
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
    <div class="spinner-container absolute top-0 right-0 opacity-0">
      <Spinner smaller={true} />
    </div>
  {/if}
</button>
