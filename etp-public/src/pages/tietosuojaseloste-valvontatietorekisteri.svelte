<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteValvontatietorekisteriFi from '@Page/tietosuojaseloste-valvontatietorekisteri-fi';
</script>

{#if $locale === 'sv'}
  <p>TBD</p>
{:else}
  <TietosuojaselosteValvontatietorekisteriFi />
{/if}
