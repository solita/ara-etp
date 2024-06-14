<script>
  import { locale } from '@Localization/localization';
  import RekisteroitumisohjeetSv from '@Page/rekisteroitumisohjeet-sv';
  import RekisteroitumisohjeetFi from '@Page/rekisteroitumisohjeet-fi';
</script>

{#if $locale === 'sv'}
  <RekisteroitumisohjeetSv />
{:else}
  <RekisteroitumisohjeetFi />
{/if}
