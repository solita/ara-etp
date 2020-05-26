<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;

  const center = false;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.lkvn-kaytto.header')} />
<div class="flex lg:flex-row flex-col">
<div class="w-1/2 py-4 mb-4 flex flex-row items-end">
  <div class="w-3/5">
    <Input
        {disabled}
        {schema}
        {center}
        bind:model={energiatodistus}
        path={['lahtotiedot', 'lkvn-kaytto', 'kulutus-per-nelio']} />
  </div>
  <div class="w-2/5 pl-2 pr-20">{$_('energiatodistus.litraa-per-neliovuosi')}</div>
</div>
<div class="w-1/2 py-4 mb-4 flex flex-row items-end">
  <div class="w-3/5">
    <Input
      {disabled}
      {schema}
      {center}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lkvn-kaytto', 'vuosikulutus']} />
  </div>
  <div class="w-2/5 pl-2">{$_('energiatodistus.vuosikulutus-per-nelio')}</div>
</div>
</div>