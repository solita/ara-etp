<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  export let model;
  export let lens = R.identity;
  export let showConjugation = false;
  export let block = [];

  export let remove;

  let group = 'and';

  $: value = R.view(lens, model);

  $: console.log(value);
</script>

<style>

</style>

{#if showConjugation}
  <div class="flex justify-between w-1/6 my-10">
    <Radio bind:group value={'and'} label={$_('energiatodistus.haku.and')} />
    <Radio bind:group value={'or'} label={$_('energiatodistus.haku.or')} />
  </div>
{/if}

<div class="w-3/4 flex-grow flex">
  <div class="flex-grow mr-4">
    <Select
      model={{ value: Maybe.None() }}
      lens={R.lensProp('value')}
      allowNone={true} />
  </div>
  <div class="flex-grow">
    <Select
      model={{ value: Maybe.None() }}
      lens={R.lensProp('value')}
      allowNone={true} />
  </div>
</div>
