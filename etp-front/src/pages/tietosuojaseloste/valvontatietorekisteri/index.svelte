<script>
  import { locale } from '@Language/i18n';
  import TietosuojaselosteFi from './fi';
</script>

<div>
  {#if $locale.startsWith('sv')}
    <p>TBD</p>
  {:else}
    <TietosuojaselosteFi />
  {/if}
</div>
