<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  export let laskutustieto;
</script>

<style type="text/postcss">
  .inner {
    @apply flex justify-between w-1/2;
  }

  .inner:not(:last-child) {
    @apply mb-16;
  }
</style>

<div class="w-full" transition:slide={{ duration: 200 }}>
  <span class="uppercase font-bold">Yrityksen perustiedot</span>
  <div class="inner">
    <span>{laskutustieto.nimi}</span>
    <span>{laskutustieto.ytunnus}</span>
  </div>
  <span class="uppercase font-bold">Verkkolaskuosoite</span>
  <div class="inner">
    <span>{laskutustieto.verkkolaskuosoite}</span>
  </div>
</div>
