<script>
  import { locale } from '@Language/i18n';
  import TietosuojaselosteFi from './fi';
  import TietosuojaselosteSv from './sv';
</script>

<div>
  {#if $locale.startsWith('sv')}
    <TietosuojaselosteSv />
  {:else}
    <TietosuojaselosteFi />
  {/if}
</div>
