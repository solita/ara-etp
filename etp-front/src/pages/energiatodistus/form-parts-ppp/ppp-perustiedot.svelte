<script>
  import * as R from 'ramda';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import HR from '@Component/HR/HR';
  import Select from '@Component/Select/Select';
  import { locale, _ } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as et from '@Pages/energiatodistus/energiatodistus-utils';
  import BasicInput from '@Component/Input/Input';
  import Input from '@Pages/energiatodistus/Input';

  export let perusparannuspassi;
  export let schema;
  export let luokittelut;
  export let disabled = false;

  $: labelLocale = LocaleUtils.label($locale);
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
  <Select
    id={'perusparannuspassi.passin-perustiedot.a0-merkinta-id'}
    name={'perusparannuspassi.passin-perustiedot.a0-merkinta-id'}
    label={$_('perusparannuspassi.passin-perustiedot.a0-merkinta-id')}
    allowNone={false}
    {disabled}
    bind:model={perusparannuspassi}
    lens={R.lensPath(['passin-perustiedot', 'a0-merkinta-id'])}
    format={et.selectFormat(labelLocale, luokittelut.a0Merkinta)}
    items={R.pluck('id', luokittelut.a0Merkinta)} />

  <Checkbox
    {disabled}
    bind:model={perusparannuspassi}
    lens={R.lensPath(['passin-perustiedot', 'tayttaa-aplus-vaatimukset'])}
    label={$_(
      'perusparannuspassi.passin-perustiedot.tayttaa-aplus-vaatimukset'
    )} />
  <HR />
</div>
