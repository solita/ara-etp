<script>
  export let text = 'Example';
</script>

<style type="text/postcss">
  .example {
    @apply bg-primary w-64 text-white py-2 text-center rounded-lg;
  }
</style>

<div class="example">{text}</div>
