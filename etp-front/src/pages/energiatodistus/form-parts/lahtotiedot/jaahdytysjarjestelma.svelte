<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={$_('energiatodistus.lahtotiedot.jaahdytysjarjestelma.header')} />
<div class="mb-4 w-1/3 py-4">
  <Input
    {disabled}
    {schema}
    center={false}
    bind:model={energiatodistus}
    path={[
      'lahtotiedot',
      'jaahdytysjarjestelma',
      'jaahdytyskauden-painotettu-kylmakerroin'
    ]} />
</div>
