<script>
  export let text = '';
</script>

<style type="text/postcss">
  .error {
    @apply flex py-2 px-2 bg-error text-light;
  }
</style>

<div class="my-2 error">
  <span class="font-icon mr-2">error_outline</span>
  <div role="alert">{text}</div>
</div>
