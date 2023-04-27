<script>
  export let legendText;
</script>

<style type="text/postcss">
  legend {
    @apply text-secondary;
  }
</style>

<fieldset class="flex flex-col md:flex-row py-4">
  <legend>{legendText}</legend>
  <slot />
</fieldset>
