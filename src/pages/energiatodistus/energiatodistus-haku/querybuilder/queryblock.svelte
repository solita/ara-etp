<script>
  import { tick } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _ } from '@Language/i18n';

  import * as Inputs from '@Pages/energiatodistus/inputs';

  import QueryInput from './query-input';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import Input from '@Component/Input/Input';

  export let operator;
  export let key;
  export let values;
  export let index;
  export let schema;
  export let luokittelut;
  export let laatijat;

  let input;

  // There is pretty strong coupling in Inputs.propertyLabel for energiatodistus-namespace
  // We could generalize this for laatijat also.
  const keyLabel = R.cond([
    [
      R.compose(R.equals('energiatodistus'), R.head, R.split('.')),
      R.compose(Inputs.propertyLabel($_), R.join('.'), R.tail, R.split('.'))
    ],
    [
      R.compose(R.equals('laatija'), R.head, R.split('.')),
      R.compose($_, R.concat(R.__, '_haku'))
    ],
    [R.T, $_]
  ]);

  const findKey = label =>
    R.compose(
      Maybe.fromNull,
      R.find(R.compose(R.equals(label), keyLabel)),
      R.keys
    )(schema);

  const findLabel = R.compose(
    Maybe.orSome(''),
    R.map(keyLabel),
    Maybe.fromEmpty
  );

  let previousType;

  $: completedValue = findLabel(key);

  $: maybeKey = findKey(completedValue);

  $: if (Maybe.isSome(maybeKey)) {
    tick().then(() => {
      input.value = Maybe.get(maybeKey);
      input.dispatchEvent(new Event('change', { bubbles: true }));
    });
  }

  $: operations = Maybe.orSome([], R.map(R.prop(R.__, schema), maybeKey));

  $: op = R.compose(
    Maybe.orSome(R.head(operations)),
    Maybe.fromNull,
    R.find(R.pathEq(['operation', 'browserCommand'], operator))
  )(operations);

  $: if (op && values.length === 0) {
    values = op.defaultValues();
  }

  $: if (op && previousType !== undefined && previousType !== op.type) {
    values = op.defaultValues();
    previousType = op.type;
  } else if (op) {
    previousType = op.type;
  }
</script>

<div class="flex items-end justify-start my-8 w-full">
  <div class="w-1/2 mr-4">
    <Autocomplete
      bind:completedValue
      items={R.compose(R.map(keyLabel), R.keys)(schema)}
      size={100000}>
      <Input
        bind:model={completedValue}
        label={$_('energiatodistus.haku.valitse_hakuehto')}
        search={true} />
    </Autocomplete>
    <input
      bind:this={input}
      tabindex="-1"
      class="sr-only"
      name={`${index}_key`} />
  </div>
  {#if Maybe.isSome(maybeKey) && values.length > 0}
    <div class="w-1/2">
      <QueryInput
        nameprefix={`${index}`}
        operation={operator}
        {values}
        operations={Maybe.orSome([], R.map(R.prop(R.__, schema), maybeKey))}
        {luokittelut}
        {laatijat} />
    </div>
  {/if}
</div>
