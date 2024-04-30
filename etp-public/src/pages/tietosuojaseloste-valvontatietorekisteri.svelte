<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteValvontatietorekisteriFi from '@Page/tietosuojaseloste-valvontatietorekisteri-fi';
  import TietosuojaselosteValvontatietorekisteriSv from '@Page/tietosuojaseloste-valvontatietorekisteri-sv';
</script>

{#if $locale === 'sv'}
  <TietosuojaselosteValvontatietorekisteriSv />
{:else}
  <TietosuojaselosteValvontatietorekisteriFi />
{/if}
