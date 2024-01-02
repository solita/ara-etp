<script>
  import { _, locale } from '@Localization/localization';
  import AineistopalveluLiittyminenFi from '@Page/aineistopalvelu-liittyminen-fi';
  import AineistopalveluLiittyminenSv from '@Page/aineistopalvelu-liittyminen-sv';
</script>

{#if $locale === 'sv'}
  <AineistopalveluLiittyminenSv />
{:else}
  <AineistopalveluLiittyminenFi />
{/if}
