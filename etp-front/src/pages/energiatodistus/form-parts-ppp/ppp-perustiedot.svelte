<script>
  import * as R from 'ramda';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import HR from '@Component/HR/HR';
  import { _ } from '@Language/i18n';
  import BasicInput from '@Component/Input/Input';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
</script>

<div class="flex min-w-full flex-col gap-y-5 overflow-x-auto">
  {#if R.complement(R.isNil)(perusparannuspassi.id)}
    <BasicInput
      id="perusparannuspassi.id"
      name="perusparannuspassi.id"
      label={$_('perusparannuspassi.id')}
      disabled={true}
      bind:model={perusparannuspassi}
      lens={R.lensProp('id')}
      i18n={$_} />
  {/if}
  <div class="flex flex-col gap-x-8 lg:flex-row">
    <div class="min-w-0 flex-1">
      <Input
        bind:model={perusparannuspassi}
        i18nRoot="perusparannuspassi"
        path={['passin-perustiedot', 'havainnointikaynti']}
        {schema} />
    </div>

    <div class="min-w-0 flex-1">
      <Input
        bind:model={perusparannuspassi}
        i18nRoot="perusparannuspassi"
        path={['passin-perustiedot', 'passin-esittely']}
        {schema} />
    </div>
  </div>
  <Checkbox
    bind:model={perusparannuspassi}
    lens={R.lensPath(['passin-perustiedot', 'tayttaa-a0-vaatimukset'])}
    label={$_(
      'perusparannuspassi.passin-perustiedot.tayttaa-a0-vaatimukset'
    )} />

  <Checkbox
    bind:model={perusparannuspassi}
    lens={R.lensPath(['passin-perustiedot', 'tayttaa-aplus-vaatimukset'])}
    label={$_(
      'perusparannuspassi.passin-perustiedot.tayttaa-aplus-vaatimukset'
    )} />
  <HR />
</div>
