<script>
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';

  import H3 from '@Component/H/H3';
  import Textarea from '@Component/Energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let inputLanguage;
  export let versio;
</script>

<H3 text={$_(`energiatodistus.huomiot.suositukset-header.${versio}`)} />
<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    inputLanguage={Maybe.Some(inputLanguage)}
    path={['huomiot', 'suositukset']} />
</div>

<H3 text={$_('energiatodistus.huomiot.lisatietoja-header')} />
<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    bind:model={energiatodistus}
    inputLanguage={Maybe.Some(inputLanguage)}
    path={['huomiot', 'lisatietoja']} />
</div>
