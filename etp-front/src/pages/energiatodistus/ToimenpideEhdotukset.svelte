<script>
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';

  import H2 from '@Component/H/H2';
  import H3 from '@Component/H/H3';

  import Textarea from '@Pages/energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let inputLanguage;
  export let energiatodistus;
  export let versio;
</script>

<style>
</style>

<H2
  text={$_(
    `energiatodistus.perustiedot.keskeiset-suositukset-heading.${versio}.h2`
  )} />
<H3
  text={$_(
    `energiatodistus.perustiedot.keskeiset-suositukset-heading.${versio}.h3`
  )} />

<div class="w-full py-4 mb-4">
  <Textarea
    {disabled}
    {schema}
    inputLanguage={Maybe.Some(inputLanguage)}
    bind:model={energiatodistus}
    path={['perustiedot', 'keskeiset-suositukset']} />
</div>
