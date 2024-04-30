<script>
  import { locale } from '@Language/i18n';
  import Tietosuojaseloste from './fi';
</script>

<div>
  {#if $locale.startsWith('sv')}
    <p>TBD</p>
  {:else}
    <Tietosuojaseloste />
  {/if}
</div>
