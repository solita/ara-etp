<script>
  export let compact = false;
</script>

<style type="text/postcss">
  hr {
    @apply my-12 border-t-7 border-primary opacity-15;
  }

  hr.compact {
    @apply my-8;
  }
</style>

<hr class:compact />
