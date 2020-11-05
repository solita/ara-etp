<script>
  import { tick } from 'svelte';
  import qs from 'qs';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  import * as Inputs from '@Component/Energiatodistus/inputs';
  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';

  import QueryInput from './query-input';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import SimpleInput from '@Component/Input/SimpleInput';

  export let operator;
  export let key;
  export let values;
  export let index;
  export let schema;

  let input;

  const findKey = label =>
    R.compose(
      Maybe.fromNull,
      R.find(
        R.compose(
          R.equals(label),
          Inputs.propertyLabel($_)
        )
      ),
      R.keys
    )(schema);

  const findLabel = R.compose(
    Maybe.orSome(''),
    R.map(Inputs.propertyLabel($_)),
    Maybe.fromEmpty
  );

  $: completedValue = findLabel(key);

  $: maybeKey = findKey(completedValue);

  $: if (Maybe.isSome(maybeKey)) {
    tick().then(() => {
      input.value = Maybe.get(maybeKey);
      input.dispatchEvent(new Event('change', { bubbles: true }));
    });
  }
</script>

<div class="flex-grow flex items-center justify-start my-8">
  <div class="w-1/2 mr-4">
    <Autocomplete
      bind:completedValue
      items={R.compose( R.map(Inputs.propertyLabel($_)), R.keys )(schema)}
      size={100000} />
    <input
      bind:this={input}
      tabindex="-1"
      class="sr-only"
      name={`${index}_key`} />
  </div>
  {#if Maybe.isSome(maybeKey)}
    <div class="w-1/2">
      <QueryInput
        nameprefix={`${index}`}
        operation={operator}
        {values}
        operations={Maybe.orSome([], R.map(R.prop(R.__, schema), maybeKey))} />
    </div>
  {/if}
</div>
