<script>
  import { _, locale } from '@Localization/localization';
  import EnergiatodistusrekisterinRajapintapalveluFi from '@Page/energiatodistusrekisterin-rajapintapalvelu-fi';
  import EnergiatodistusrekisterinRajapintapalveluSv from '@Page/energiatodistusrekisterin-rajapintapalvelu-sv';
</script>

{#if $locale === 'sv'}
  <EnergiatodistusrekisterinRajapintapalveluSv />
{:else}
  <EnergiatodistusrekisterinRajapintapalveluFi />
{/if}
