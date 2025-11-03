<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Pages/energiatodistus/form-parts/units/annual-energy-over-area';
  import LitraaPerAlaUnit from '@Pages/energiatodistus/form-parts/units/annual-volume-over-area';

  export let disabled;
  export let schema;
  export let energiatodistus;

  const center = false;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.lkvn-kaytto.header')} />
<div class="flex flex-col lg:flex-row">
  <div class="mb-12 flex w-1/2 flex-row items-end py-4">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lkvn-kaytto', 'ominaiskulutus']}
      unit={LitraaPerAlaUnit} />
  </div>
  <div class="mb-12 flex w-1/2 flex-row items-end py-4">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lkvn-kaytto', 'lammitysenergian-nettotarve']}
      unit={VuosikulutusPerAlaUnit} />
  </div>
</div>
