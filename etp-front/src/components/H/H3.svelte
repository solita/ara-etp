<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h3 {
    @apply mb-6 mt-0 w-full text-h3 font-bold uppercase leading-header tracking-xl text-secondary;
  }

  .compact {
    @apply m-0;
  }
</style>

<h3 class:compact>{text}</h3>
