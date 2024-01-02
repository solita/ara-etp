<script>
  import { locale } from '@Localization/localization';
  import SaavutettavuusselosteFi from '@Page/saavutettavuusseloste-fi';
  import SaavutettavuusselosteSv from '@Page/saavutettavuusseloste-sv';
</script>

{#if $locale === 'sv'}
  <SaavutettavuusselosteSv />
{:else}
  <SaavutettavuusselosteFi />
{/if}
