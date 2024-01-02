<script>
  import { _ } from '@Language/i18n';

  export let id;
  export let required;
  export let error;
  export let warning;
  export let focused;
  export let label = '';
  export let compact = false;

  export let unit;

  const i18n = $_;
</script>

<style type="text/postcss">
  label {
    @apply text-secondary;
  }

  .required-marker {
    @apply font-icon text-xs align-top mr-1;
  }

  label.warning,
  label.warning::before {
    @apply text-dark;
  }

  label.error,
  label.error::before {
    @apply text-error;
  }

  label.focused,
  label.focused::before {
    @apply font-bold;
  }
</style>

<!-- purgecss: sr-only required error focused warning -->
<label
  for={id}
  class:sr-only={compact}
  class:required
  class:warning
  class:error
  class:focused>
  {#if required}
    <span class="required-marker" aria-hidden="true">*</span>
  {/if}
  {label}
  {#if required}
    <span class="sr-only">({i18n('validation.required')})</span>
  {/if}
  <svelte:component this={unit} />
</label>
