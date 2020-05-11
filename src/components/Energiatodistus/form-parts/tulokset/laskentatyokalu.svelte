<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3
  compact={true}
  text={$_('energiatodistus.tulokset.laskentatyokalu_header')} />

<div class="w-full py-4 mb-4">
  <Input
    {disabled}
    {schema}
    center={false}
    bind:model={energiatodistus}
    path={['tulokset', 'laskentatyokalu']} />
</div>
