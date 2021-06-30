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
    @apply flex relative items-center border-b-3 border-disabled text-dark;
  }

  div:hover {
    @apply border-hover;
  }

  div.caret::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    content: 'expand_more';
  }

  div.search::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    content: 'search';
  }

  div.focused {
    @apply border-primary;
  }

  div.focused::after {
    @apply text-primary;
  }

  div.warning {
    @apply border-warning;
  }

  div.warning::after {
    @apply text-warning;
  }

  div.error {
    @apply border-error;
  }

  div.error::after {
    @apply text-error;
  }

  div.disabled {
    @apply border-transparent;
  }
</style>

<!-- purgecss: caret search focused error disabled warning -->
<div
  class="inputwrapper"
  class:caret={caret && !disabled}
  class:search={search && !disabled}
  class:focused
  class:error
  class:disabled
  class:warning>
  <slot />
</div>
