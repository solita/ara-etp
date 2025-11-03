<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h4 {
    @apply mb-2 mt-0 w-full text-h4 font-bold uppercase leading-header text-secondary;
  }

  .compact {
    @apply m-0;
  }
</style>

<h4 class:compact>{text}</h4>
