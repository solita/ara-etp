<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';
  import { OPERATOR_TYPES } from '@Component/energiatodistus-haku/schema';
  import * as dfns from 'date-fns';

  import Select from '@Component/Select/Select';
  import SimpleInput from '@Component/Input/SimpleInput';
  import DatePicker from '@Component/Datepicker/Datepicker';
  import Radio from '@Component/Radio/Radio';

  import BooleanInput from './query-inputs/boolean-input';
  import TextInput from './query-inputs/text-input';
  import DateInput from './query-inputs/date-input';
  import NumberInput from './query-inputs/number-input';

  import { _ } from '@Language/i18n';

  export let nameprefix = '';

  export let operations;
  export let operation;
  export let values;

  const inputForType = type => {
    switch (type) {
      case OPERATOR_TYPES.NUMBER:
        return NumberInput;
      case OPERATOR_TYPES.BOOLEAN:
        return BooleanInput;
      default:
        return TextInput;
    }
  };

  $: op = R.compose(
    Maybe.orSome(R.head(operations)),
    Maybe.fromNull,
    R.find(R.pathEq(['operation', 'browserCommand'], operation))
  )(operations);

  const isOperatorType = R.curry((op, value) => {
    switch (op.type) {
      case OPERATOR_TYPES.NUMBER:
        return !isNaN(parseInt(value));
      case OPERATOR_TYPES.BOOLEAN:
        return typeof value === 'boolean';
      case OPERATOR_TYPES.DATE:
        return true;
      default:
        return true;
    }
  });

  $: if (!isOperatorType(op, R.head(values))) {
    values = op.defaultValues();
  }
</script>

<style type="text/postcss">
  .inputs > div:not(:first-child) {
    @apply ml-2;
  }
</style>

<div class="flex">
  <input class="sr-only" name={`${nameprefix}_type`} value={op.type} />
  {#if op.type !== OPERATOR_TYPES.BOOLEAN}
    <div class="w-1/2">
      <Select
        items={R.map(R.path(['operation', 'browserCommand']), operations)}
        model={R.path(['operation', 'browserCommand'], op)}
        lens={R.identity}
        format={R.compose( $_, R.concat('energiatodistus.haku.') )}
        allowNone={false}
        name={`${nameprefix}_operation`} />
    </div>
  {:else}
    <input class="sr-only" name={`${nameprefix}_operation`} value="=" />
  {/if}
  <div class="inputs w-1/2 pl-4 flex justify-between">
    {#each values as value, index}
      <div class="flex flex-col justify-end">
        <svelte:component
          this={inputForType(op.type)}
          {value}
          {nameprefix}
          {index} />
      </div>
    {/each}
  </div>
</div>
