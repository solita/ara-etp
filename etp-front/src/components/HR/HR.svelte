<script>
  export let compact = false;
</script>

<style type="text/postcss">
  hr {
    @apply mb-12 mt-24 border-primary border-t-7 opacity-15;
  }

  hr.compact {
    @apply mb-8 mt-8;
  }
</style>

<hr class:compact />
