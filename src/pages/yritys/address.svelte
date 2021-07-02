<script>
  export let address;
</script>

<style type="text/postcss">
  address {
    @apply not-italic;
  }
</style>

<address>
  {address.jakeluosoite},
  <span class="whitespace-no-wrap">
    {address.postinumero}
    {address.postitoimipaikka}
  </span>
</address>
