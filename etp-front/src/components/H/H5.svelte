<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h5 {
    @apply text-h5 mb-2 mt-0 w-full font-bold uppercase leading-header text-secondary;
  }

  .compact {
    @apply m-0;
  }
</style>

<h5 class:compact>{text}</h5>
