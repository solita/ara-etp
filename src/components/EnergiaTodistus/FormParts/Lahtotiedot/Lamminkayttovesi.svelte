<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={'Lämmin käyttövesi'} />
<div class="w-1/3 py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'lkvn-kaytto', 'vuosikulutus']} />
</div>

<div class="w-1/3 py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'lkvn-kaytto', 'kulutus-per-nelio']} />
</div>
