<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteFi from '@Page/tietosuojaseloste-energiatodistusrekisteri-fi.svelte';
  import TietosuojaselosteSv from '@Page/tietosuojaseloste-energiatodistusrekisteri-sv.svelte';
</script>

{#if $locale === 'sv'}
  <TietosuojaselosteSv />
{:else}
  <TietosuojaselosteFi />
{/if}
