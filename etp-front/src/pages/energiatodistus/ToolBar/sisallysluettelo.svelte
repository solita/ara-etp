<script>
  import { _ } from '@Language/i18n';
  import * as Future from '@Utility/future-utils';
  import * as versionApi from '@Component/Version/version-api';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';

  const i18n = $_;

  // Load config directly (like laatijat.svelte pattern)
  let config = {};
  Future.fork(
    _ => {
      config = {}; // Fallback to empty config on error
    },
    loadedConfig => {
      config = loadedConfig;
    },
    versionApi.getConfig
  );

  const allTocItems = [
    {
      id: 'perustiedot',
      label: i18n('energiatodistus.perustiedot-header')
    },
    {
      id: 'laskennan-lahtotiedot',
      label: i18n('energiatodistus.lahtotiedot.header')
    },
    {
      id: 'tulokset',
      label: i18n('energiatodistus.tulokset.header')
    },
    {
      id: 'toteutunut-ostoenergiankulutus',
      label: i18n('energiatodistus.toteutunut-ostoenergiankulutus.header')
    },
    {
      id: 'toimenpide-ehdotukset',
      label: i18n('energiatodistus.huomiot.header.2018')
    },
    {
      id: 'lisamerkintoja',
      label: i18n('energiatodistus.lisamerkintoja')
    },
    {
      id: 'perusparannuspassi',
      label: i18n('energiatodistus.perusparannuspassi.header'),
      requiresEtp2026: true
    }
  ];

  $: tocItems = allTocItems.filter(
    item => !item.requiresEtp2026 || isEtp2026Enabled(config)
  );

  const scrollToSection = id => {
    const element = document.getElementById(id);
    if (element) {
      // Use getBoundingClientRect to get accurate position relative to viewport
      const elementRect = element.getBoundingClientRect();
      const currentScrollY =
        window.pageYOffset || document.documentElement.scrollTop;
      const targetPosition = elementRect.top + currentScrollY - 20;

      window.scrollTo({
        top: targetPosition,
        behavior: 'smooth'
      });
    }
  };
</script>

<style type="text/postcss">
  .toc {
    @apply flex w-[240px] flex-col;
  }

  .toc-item {
    @apply block py-2 text-sm;
    color: #0000ee; /* Default browser link blue */
    text-decoration: underline;
  }

  .toc-item:visited {
    color: #551a8b; /* Default browser visited link purple */
  }

  .toc-item:hover {
    color: #0000ee;
    text-decoration: underline;
  }

  .toc-header {
    @apply font-semibold leading-header text-dark;
  }
</style>

<div class="toc mb-6">
  <div class="toc-header">
    {i18n('energiatodistus.toolbar.sisallysluettelo')}
  </div>

  {#each tocItems as item}
    <a
      class="toc-item"
      href="#{item.id}"
      on:click|preventDefault={() => scrollToSection(item.id)}>
      {item.label}
    </a>
  {/each}
</div>
