<script>
  export let text = '';
</script>

<style type="text/postcss">
  .error {
    @apply flex bg-error px-2 py-2 text-light;
  }
</style>

<div class="error my-2">
  <span class="mr-2 font-icon">error_outline</span>
  <div role="alert">{text}</div>
</div>
