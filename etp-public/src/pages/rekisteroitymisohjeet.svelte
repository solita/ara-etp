<script>
  import { _, locale } from '@Localization/localization';
  import RekisteroitumisohjeetSv from '@Page/rekisteroitumisohjeet-sv';
  import RekisteroitumisohjeetFi from '@Page/rekisteroitumisohjeet-fi';
  import Seo from '@Component/seo.svelte';
  import { announceAssertively } from '@/utilities/announce';
  import { onMount } from 'svelte';

  onMount(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });
  });

  announceAssertively($_('REKISTEROITYMISOHJEET'));
</script>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('REKISTEROITYMISOHJEET')}"
  descriptionFi={$locale == 'fi' ? $_('REOHJ_INFO_TITLE') : undefined}
  descriptionSv={$locale == 'sv' ? $_('REOHJ_INFO_TITLE') : undefined} />

{#if $locale === 'sv'}
  <RekisteroitumisohjeetSv />
{:else}
  <RekisteroitumisohjeetFi />
{/if}
