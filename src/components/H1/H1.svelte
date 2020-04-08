<script>
  export let text = '';
</script>

<style type="text/postcss">
  h1 {
    @apply text-secondary font-bold uppercase text-lg pb-8;
    letter-spacing: 0.15em; /* TODO: Generate in tailwind */
  }
</style>

<h1>{text}</h1>
