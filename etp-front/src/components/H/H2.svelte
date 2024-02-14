<script>
  export let dataCy;
  export let text = '';
</script>

<style type="text/postcss">
  h2 {
    @apply text-primary uppercase font-bold text-base mb-4;
  }
</style>

<h2 data-cy={dataCy}>{text}</h2>
