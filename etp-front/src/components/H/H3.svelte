<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h3 {
    @apply text-secondary w-full font-bold uppercase text-h3 mt-0 mb-4 tracking-xl leading-header;
  }

  .compact {
    @apply m-0;
  }
</style>

<h3 class:compact>{text}</h3>
