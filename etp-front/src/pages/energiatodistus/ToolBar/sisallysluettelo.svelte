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
      const targetPosition = elementRect.top + currentScrollY - 20;

      window.scrollTo({
        top: targetPosition,
        behavior: 'smooth'
      });
    } else {
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
