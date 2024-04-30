<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselostAineistoPalvelunKayttajaRekisteriFi from '@Page/tietosuojaseloste-aineistopalvelun-kayttajarekisteri-fi';
  import TietosuojaselostAineistoPalvelunKayttajaRekisteriSv from '@Page/tietosuojaseloste-aineistopalvelun-kayttajarekisteri-sv';
</script>

{#if $locale === 'sv'}
  <TietosuojaselostAineistoPalvelunKayttajaRekisteriSv />
{:else}
  <TietosuojaselostAineistoPalvelunKayttajaRekisteriFi />
{/if}
