<script>
  import { _ } from '@Language/i18n';

  const i18n = $_;

  const tocItems = [
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
    }
  ];

  const scrollToSection = id => {
    const element = document.getElementById(id);
    if (element) {
      // Use getBoundingClientRect to get accurate position relative to viewport
      const elementRect = element.getBoundingClientRect();
      const currentScrollY =
        window.pageYOffset || document.documentElement.scrollTop;
      // Calculate position with 20px offset above the element
      const targetPosition = elementRect.top + currentScrollY - 20;

      window.scrollTo({
        top: targetPosition,
        behavior: 'smooth'
      });
    } else {
      // Debug: let's see all elements with IDs
      const allElementsWithIds = document.querySelectorAll('[id]');
      console.log(
        'All elements with IDs:',
        Array.from(allElementsWithIds).map(el => el.id)
      );
    }
  };
</script>

<style type="text/postcss">
  .toc {
    @apply flex flex-col w-[240px];
  }

  .toc-item {
    @apply w-full py-2 text-left text-sm cursor-pointer;
    @apply text-secondary text-tocLink underline leading-header;
    @apply transition-colors duration-200;
  }

  .toc-item:active {
    @apply bg-primarydark;
  }

  .toc-header {
    @apply font-semibold text-dark leading-header;
  }
</style>

<div class="toc">
  <div class="toc-header">
    {i18n('energiatodistus.toolbar.sisallysluettelo')}
  </div>

  {#each tocItems as item}
    <button
      class="toc-item"
      on:click={() => scrollToSection(item.id)}
      type="button">
      {item.label}
    </button>
  {/each}
</div>
