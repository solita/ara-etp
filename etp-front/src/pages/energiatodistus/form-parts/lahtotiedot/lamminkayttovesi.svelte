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
<div class="flex lg:flex-row flex-col">
  <div class="w-1/2 py-4 mb-4 flex flex-row items-end">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lkvn-kaytto', 'ominaiskulutus']}
      unit={LitraaPerAlaUnit} />
  </div>
  <div class="w-1/2 py-4 mb-4 flex flex-row items-end">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lkvn-kaytto', 'lammitysenergian-nettotarve']}
      unit={VuosikulutusPerAlaUnit} />
  </div>
</div>
