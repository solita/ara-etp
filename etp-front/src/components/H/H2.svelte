<script>
  export let dataCy;
  export let text = '';
  export let id;
</script>

<style type="text/postcss">
  h2 {
    @apply text-primary uppercase font-bold text-h2 mb-12 leading-header;
  }
</style>

<h2 {id} data-cy={dataCy}>{text}</h2>
