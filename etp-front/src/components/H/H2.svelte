<script>
  export let dataCy;
  export let text = '';
  export let id;
</script>

<style type="text/postcss">
  h2 {
    @apply mb-12 text-h2 font-bold uppercase leading-header text-primary;
  }
</style>

<h2 {id} data-cy={dataCy}>{text}</h2>
