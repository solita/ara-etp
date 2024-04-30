<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselostAineistoPalvelunKayttajaRekisteriFi from '@Page/tietosuojaseloste-aineistopalvelun-kayttajarekisteri-fi';
</script>

{#if $locale === 'sv'}
  <p>TBD</p>
{:else}
  <TietosuojaselostAineistoPalvelunKayttajaRekisteriFi />
{/if}
