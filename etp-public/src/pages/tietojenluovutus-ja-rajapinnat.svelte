<script>
  import { _, locale } from '@Localization/localization';
  import TietojenluovutusJaRajapinnatFi from '@Page/tietojenluovutus-ja-rajapinnat-fi';
  import TietojenluovutusJaRajapinnatSv from '@Page/tietojenluovutus-ja-rajapinnat-sv';
</script>

{#if $locale === 'sv'}
  <TietojenluovutusJaRajapinnatSv />
{:else}
  <TietojenluovutusJaRajapinnatFi />
{/if}
