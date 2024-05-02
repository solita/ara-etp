<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';

  let component;
  announceAssertively($_('TIETOA_SIVUSTOSTA'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
</script>

<style>
  ul {
    list-style: disc;
    @apply my-4;
  }
  li {
    @apply ml-6;
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('TIETOA_SIVUSTOSTA')}"
  descriptionFi={$locale == 'fi' ? $_('TIETOA_SIVUSTOSTA') : undefined}
  descriptionSv={$locale == 'sv' ? $_('TIETOA_SIVUSTOSTA') : undefined} />

<Container {...containerStyles.white}>
  <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto" bind:this={component}>
    <div>
      <h1 class="mb-4">{$_('TIETOA_SIVUSTOSTA')}</h1>
      <ul>
        <li>
          <a href="/saavutettavuusseloste" class="text-darkgreen">
            {$_('SAAVUTETTAVUUSSELOSTE')}
          </a>
        </li>
        <li>
          <a
            href="/tietosuojaseloste-energiatodistusrekisteri"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_ENERGIATODISTUSREKISTERI')}
          </a>
        </li>
        <li>
          <a href="/tietosuojaseloste-laatijarekisteri" class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_LAATIJAREKISTERI')}
          </a>
        </li>
        <li>
          <a
            href="/tietosuojaseloste-valvontatietorekisteri"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_VALVONTATIETOREKISTERI')}
          </a>
        </li>
        <li>
          <a
            href="/tietosuojaseloste-aineistopalvelun-kayttajarekisteri"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_AINEISTOPALVELUN_KAYTTAJAREKISTERI')}
          </a>
        </li>
        <li>
          <a href="/tietojenluovutus-ja-rajapinnat" class="text-darkgreen"
            >{$_('TIETOJENLUOVUTUS_JA_RAJAPINNAT')}</a>
        </li>
      </ul>
    </div>
  </div>
</Container>
