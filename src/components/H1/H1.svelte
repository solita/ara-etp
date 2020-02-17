<script>
  export let text = '';
</script>

<style type="text/postcss">
  h1 {
    @apply text-secondary font-extrabold uppercase text-lg pt-12 pb-8;
  }
</style>

<h1>{text}</h1>
