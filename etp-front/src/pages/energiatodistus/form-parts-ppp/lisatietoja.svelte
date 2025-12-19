<script>
  import * as Maybe from '@Utility/maybe-utils';

  import H3 from '@Component/H/H3';
  import { _ } from '@Language/i18n';
  import Textarea from '@Pages/energiatodistus/Textarea.svelte';

  const i18n = $_;

  export let perusparannuspassi;
  export let schema;
  export let inputLanguage;
</script>

<div class="mb-4 w-full py-4">
  <div class="py-4">
    <H3
      text={i18n(`perusparannuspassi.passin-perustiedot.lisatiedot.header`)} />
  </div>
  <Textarea
    {schema}
    i18nRoot="perusparannuspassi"
    bind:model={perusparannuspassi}
    path={['passin-perustiedot', 'lisatietoja']}
    inputLanguage={Maybe.Some(inputLanguage)} />
</div>
