<script>
  export let text = '';
</script>

<style type="text/postcss">
  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-8 tracking-xl;
  }
</style>

<h1>{text}</h1>
