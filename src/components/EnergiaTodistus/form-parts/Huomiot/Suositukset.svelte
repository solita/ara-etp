<script>
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Textarea from '@Component/Energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;
</script>

<H3 text={$_('energiatodistus.huomiot.suositukset-header')} />
<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    path={['huomiot', 'suositukset-fi']} />
</div>
