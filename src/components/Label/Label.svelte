<script>
  export let id;
  export let required;
  export let error;
  export let focused;
  export let label = '';
</script>

<style type="text/postcss">
  label {
    @apply text-secondary;
  }

  label.required::before {
    @apply font-icon text-xs align-top;
    content: '*';
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

<label for={id} class:required class:error class:focused>{label}</label>
