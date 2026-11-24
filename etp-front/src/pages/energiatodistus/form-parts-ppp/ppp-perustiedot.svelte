<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import UnescapedItem from '@Component/DropdownList/UnescapedItem.svelte';
  import HR from '@Component/HR/HR';
  import { _ } from '@Language/i18n';
  import BasicInput from '@Component/Input/Input';
  import Input from '@Pages/energiatodistus/Input';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';

  export let perusparannuspassi;
  export let schema;
  export let disabled = false;
  export let a0Merkinnat = [];
</script>

<div class="flex min-w-full flex-col gap-y-5">
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
        {disabled}
        bind:model={perusparannuspassi}
        i18nRoot="perusparannuspassi"
        required={true}
        path={['passin-perustiedot', 'havainnointikaynti']}
        {schema} />
    </div>

    <div class="min-w-0 flex-1">
      <Input
        {disabled}
        bind:model={perusparannuspassi}
        i18nRoot="perusparannuspassi"
        path={['passin-perustiedot', 'passin-esittely']}
        {schema} />
    </div>
  </div>
  <div class="w-full">
    <Select
      id={'passin-perustiedot.a0-merkinta-id'}
      name={'passin-perustiedot.a0-merkinta-id'}
      label={$_('perusparannuspassi.passin-perustiedot.a0-merkinta-id')}
      required={true}
      validation={schema.$signature}
      allowNone={false}
      {disabled}
      bind:model={perusparannuspassi}
      lens={R.lensPath(['passin-perustiedot', 'a0-merkinta-id'])}
      parse={Maybe.Some}
      format={ETUtils.a0MerkintaFormat($_)}
      itemComponent={UnescapedItem}
      items={R.filter(
        id => id !== ETUtils.a0Merkinta.A0 && id !== ETUtils.a0Merkinta.A0_MPR,
        R.pluck('id', a0Merkinnat)
      )} />
  </div>

  <Checkbox
    {disabled}
    bind:model={perusparannuspassi}
    lens={R.lensPath(['passin-perustiedot', 'tayttaa-aplus-vaatimukset'])}
    label={$_(
      'perusparannuspassi.passin-perustiedot.tayttaa-aplus-vaatimukset'
    )} />
  <HR />
</div>
