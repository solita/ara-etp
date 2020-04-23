<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/EnergiaTodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.jaahdytysjarjestelma.header')} />
<div class="w-full py-4 mb-4">
  <Input
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'jaahdytysjarjestelma', 'jaahdytyskauden-painotettu-kylmakerroin']} />
</div>
