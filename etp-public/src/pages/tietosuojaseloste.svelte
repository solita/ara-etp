<script>
  import { locale } from '@Localization/localization';
  import TietosuojaselosteFi from '@Page/tietosuojaseloste-fi';
</script>

{#if $locale === 'sv'}
  <p>Tietosuojaseloste ruotsiksi</p>
{:else}
  <TietosuojaselosteFi />
{/if}
