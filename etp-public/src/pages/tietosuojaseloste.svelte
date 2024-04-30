<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteFi from '@Page/tietosuojaseloste-fi';
  import TietosuojaselosteSv from '@Page/tietosuojaseloste-sv';
</script>

{#if $locale === 'sv'}
  <TietosuojaselosteSv />
{:else}
  <TietosuojaselosteFi />
{/if}
