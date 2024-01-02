<script>
  export let text = '';
  export let compact = false;
</script>

<style type="text/postcss">
  h3 {
    @apply text-secondary w-full bg-secondary text-light font-bold uppercase text-sm mt-0 mb-4 p-3 tracking-xl;
  }

  .compact {
    @apply m-0;
  }
</style>

<h3 class:compact>{text}</h3>
