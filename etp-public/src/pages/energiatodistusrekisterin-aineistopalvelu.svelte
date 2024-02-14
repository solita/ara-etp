<script>
  import { _, locale } from '@Localization/localization';
  import EnergiatodistusrekisterinAineistopalveluFi from '@Page/energiatodistusrekisterin-aineistopalvelu-fi';
  import EnergiatodistusrekisterinAineistopalveluSv from '@Page/energiatodistusrekisterin-aineistopalvelu-sv';
</script>

{#if $locale === 'sv'}
  <EnergiatodistusrekisterinAineistopalveluSv />
{:else}
  <EnergiatodistusrekisterinAineistopalveluFi />
{/if}
