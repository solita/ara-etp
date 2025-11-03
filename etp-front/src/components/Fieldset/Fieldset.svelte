<script>
  export let legendText;
</script>

<style type="text/postcss">
  legend {
    @apply text-secondary;
  }
</style>

<fieldset class="flex flex-col py-4 md:flex-row">
  <legend>{legendText}</legend>
  <slot />
</fieldset>
