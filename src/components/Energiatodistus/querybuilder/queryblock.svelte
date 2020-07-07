<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  export let model;
  export let lens;

  const conjunctionLens = R.compose(
    lens,
    R.lensProp('conjunction')
  );

  $: ({ conjunction, block } = R.view(lens, model));
</script>

<style>

</style>

<div class="w-10/12 flex flex-col">
  {#if Maybe.isSome(conjunction)}
    <div class="flex justify-between w-1/6 my-10">
      <Radio
        bind:group={conjunction}
        value={Maybe.Some('and')}
        label={$_('energiatodistus.haku.and')}
        on:click={evt => (model = R.set(conjunctionLens, Maybe.Some('and'), model))} />
      <Radio
        bind:group={conjunction}
        value={Maybe.Some('or')}
        label={$_('energiatodistus.haku.or')}
        on:click={evt => (model = R.set(conjunctionLens, Maybe.Some('or'), model))} />
    </div>
  {/if}

  <div class="flex-grow flex">
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
</div>
