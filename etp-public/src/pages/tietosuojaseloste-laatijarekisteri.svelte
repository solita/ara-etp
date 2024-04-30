<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteLaatijarekisteriFi from '@Page/tietosuojaseloste-laatijarekisteri-fi';
</script>

{#if $locale === 'sv'}
  <p>TBD</p>
{:else}
  <TietosuojaselosteLaatijarekisteriFi />
{/if}
