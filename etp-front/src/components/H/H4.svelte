<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h4 {
    @apply text-secondary w-full font-bold uppercase text-h4 mt-0 mb-2 leading-header;
  }

  .compact {
    @apply m-0;
  }
</style>

<h4 class:compact>{text}</h4>
