<script>
  export let caret;
  export let search;
  export let disabled;
  export let focused;
  export let error;
  export let warning;
</script>

<style type="text/postcss">
  div {
    @apply relative flex items-center rounded-full border-3 border-disabled px-5 text-dark;
  }

  div:hover {
    @apply border-hover bg-background;
  }

  div:hover :global(input) {
    @apply bg-background;
  }

  .focused {
    @apply border-primary;
  }

  .error {
    @apply border-error;
  }

  .disabled {
    @apply border-0 pb-3;
  }

  span {
    @apply font-icon text-2xl font-bold text-disabled;
  }
</style>

<div
  class="inputwrapper"
  class:caret
  class:search
  class:focused
  class:error
  class:warning
  class:disabled
  on:keypress>
  <slot />
  {#if search}
    <span class:focused class:error class:warning>search</span>
  {/if}
</div>
