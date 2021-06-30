<script>
  import Spinner from '@Component/Spinner/Spinner';

  export let text = '';
  export let type = 'button';
  export let style = 'primary';
  export let disabled = false;
  export let prefix = '';
  export let title = '';
  export let spinner = false;
  export let showSpinner = false;
</script>

<style type="text/postcss">
  button {
    @apply relative px-6 py-3 flex justify-center items-center rounded-full font-bold uppercase text-light tracking-xl min-w-10;
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

  .spinner {
    @apply mr-12 pr-6;

    transition: padding-right 0.5s, margin-right 0.5s;
  }

  .disabled.spinner.spinner-showing {
    @apply mr-6 pr-12;
  }

  .spinner-container {
    transition: opacity 0.5s;
  }

  .disabled .spinner-container {
    @apply opacity-100;
  }
</style>

<!-- purgecss: primary secondary error disabled pr-12 -->
<button
  {type}
  data-cy={`${prefix}-${type}`}
  class:primary={style === 'primary'}
  class:secondary={style === 'secondary'}
  class:error={style === 'error'}
  class:disabled
  class:spinner
  class:spinner-showing={showSpinner}
  {disabled}
  {title}
  on:click|stopPropagation>
  {text}
  {#if spinner && showSpinner}
    <div class="spinner-container absolute top-0 right-0 opacity-0">
      <Spinner smaller={true} />
    </div>
  {/if}
</button>
