<script>
  import { onMount } from 'svelte';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _, locale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import { announceAssertively } from '@/utilities/announce';

  import TietosuojaEtRekisteriFi from '@Asset/Tietosuojaseloste_Energiatodistusrekisteri.pdf';
  import TietosuojaEtRekisteriSv from '@Asset/Dataskyddsbeskrivning_Energicertifikatregistret.pdf';
  import TietosuojaLaatijaRekisteriFi from '@Asset/Tietosuojaseloste_Energiatodistusten_laatijarekisteri.pdf';
  import TietosuojaLaatijaRekisteriSv from '@Asset/Dataskyddsbeskrivning_Register_for_upprattare_av_energiferticikat.pdf';
  import TietosuojaValvontaRekisteriFi from '@Asset/Tietosuojaseloste_Energiatodistusten_valvontatietorekisteri.pdf';
  import TietosuojaValvontaRekisteriSv from '@Asset/Dataskyddsbeskrivning_Register_for_overvakningsuppgifter_om_energicertifikat.pdf';
  import TietosuojaAineistopalveluFi from '@Asset/tietosuoja-aineistopalvelu-fi.pdf';
  import TietosuojaAineistopalveluSv from '@Asset/tietosuoja-aineistopalvelu-sv.pdf';

  let component;
  announceAssertively($_('TIETOA_SIVUSTOSTA'));

  onMount(() => {
    component?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });

  $: [
    TietosuojaEtRekisteri,
    TietosuojaLaatijaRekisteri,
    TietosuojaValvontaRekisteri,
    TietosuojaAineistopalvelu
  ] =
    $locale === 'sv'
      ? [
          TietosuojaEtRekisteriSv,
          TietosuojaLaatijaRekisteriSv,
          TietosuojaValvontaRekisteriSv,
          TietosuojaAineistopalveluSv
        ]
      : [
          TietosuojaEtRekisteriFi,
          TietosuojaLaatijaRekisteriFi,
          TietosuojaValvontaRekisteriFi,
          TietosuojaAineistopalveluFi
        ];
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
  descriptionFi={$locale == 'fi' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined}
  descriptionSv={$locale == 'sv' ? $_('SAAVUTETTAVUUSSELOSTE') : undefined} />

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
            href={TietosuojaEtRekisteri}
            target="_blank"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_ETREKISTERI_LINK_LABEL')}
          </a>
        </li>
        <li>
          <a
            href={TietosuojaLaatijaRekisteri}
            target="_blank"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_LAATIJAREKISTERI_LINK_LABEL')}
          </a>
        </li>
        <li>
          <a
            href={TietosuojaValvontaRekisteri}
            target="_blank"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_VALVONTAREKISTERI_LINK_LABEL')}
          </a>
        </li>
        <li>
          <a
            href={TietosuojaAineistopalvelu}
            target="_blank"
            class="text-darkgreen">
            {$_('TIETOSUOJASELOSTE_AINEISTOPALVELU_LINK_LABEL')}
          </a>
        </li>
        <li>
          <a href="/aineistopalvelu-liittyminen" class="text-darkgreen"
            >{$_('AINEISTOPALVELU_LIITTYMINEN')}</a>
        </li>
      </ul>
    </div>
  </div>
</Container>
