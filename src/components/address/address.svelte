<script>
  /**
   * An html element to show any postal address.
   * e.g. the postal address of any henkilo or yritys
   */

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
