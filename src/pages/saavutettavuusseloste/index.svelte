<script>
  import { locale } from '@Language/i18n';
  import SaavutettavuusselosteFi from './fi';
  import SaavutettavuusselosteSv from './sv';
</script>

<div>
  {#if $locale.startsWith('sv')}
    <SaavutettavuusselosteSv />
  {:else}
    <SaavutettavuusselosteFi />
  {/if}
</div>
