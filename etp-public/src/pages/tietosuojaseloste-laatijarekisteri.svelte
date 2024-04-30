<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteLaatijarekisteriFi from '@Page/tietosuojaseloste-laatijarekisteri-fi';
  import TietosuojaselosteLaatijarekisteriSv from '@Page/tietosuojaseloste-laatijarekisteri-sv';
</script>

{#if $locale === 'sv'}
  <TietosuojaselosteLaatijarekisteriSv />
{:else}
  <TietosuojaselosteLaatijarekisteriFi />
{/if}
