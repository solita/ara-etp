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

  import { _ } from '@Language/i18n';

  export let operations;
  export let model;
  export let lens;

  const updateModel = R.curry((operation, values) => {
    const newBlock = EtHakuUtils.blockFromOperation(operation, values);

    if (!R.equals(R.view(lens, model), newBlock)) {
      model = R.set(lens, newBlock, model);
    }
  });

  const parseByOperationType = R.curry((operation, value) => {
    switch (operation.type) {
      case OPERATOR_TYPES.STRING:
        if (R.type(value) === 'String') {
          break;
        }
        return R.toString(value);
      case OPERATOR_TYPES.NUMBER:
        return parseFloat(value, 10);
      case OPERATOR_TYPES.DATE:
        break;
    }
    return value;
  });

  let type;
  let values;

  $: operation = R.compose(
    EtHakuUtils.operationFromBlock(operations),
    R.view(lens)
  )(model);

  $: {
    values = R.compose(
      R.map(parseByOperationType(operation)),
      R.take(operation.argumentNumber),
      R.when(
        R.always(operation.type !== type && !R.isNil(type)),
        operation.defaultValues
      ),
      R.drop(2),
      R.view(lens)
    )(model);
    type = operation.type;
  }

  $: updateModel(operation, values);
</script>

<style type="text/postcss">
  .inputs > div:not(:first-child) {
    @apply ml-2;
  }

  .radiogroup {
    @apply justify-around;
  }
</style>

<div class="flex">
  {#if operation.type !== OPERATOR_TYPES.BOOLEAN}
    <div class="w-1/2">
      <Select
        items={operations}
        bind:model={operation}
        lens={R.identity}
        format={R.compose( $_, R.concat('energiatodistus.haku.'), R.path([
            'operation',
            'browserCommand'
          ]) )}
        parse={R.identity}
        allowNone={false} />
    </div>
  {/if}
  <div class="inputs w-1/2 pl-4 flex justify-between">
    {#each values as value, index}
      <div class="flex flex-col justify-end">
        {#if operation.type === OPERATOR_TYPES.STRING || operation.type === OPERATOR_TYPES.NUMBER}
          <SimpleInput
            center={true}
            on:input={evt => (values = R.set(R.lensIndex(index), parseByOperationType(operation, evt.target.value), values))}
            viewValue={value} />
        {:else if operation.type === OPERATOR_TYPES.DATE}
          <DatePicker
            start={value}
            end={value}
            update={value => (values = R.set(R.lensIndex(index), parseByOperationType(operation, value), values))} />
        {:else if operation.type === OPERATOR_TYPES.BOOLEAN}
          <div class="radiogroup flex justify-between">
            <Radio
              group={value}
              value={true}
              on:click={evt => (values = R.set(R.lensIndex(index), true, values))}
              label={$_('energiatodistus.haku.true')} />
            <Radio
              group={value}
              value={false}
              on:click={evt => (values = R.set(R.lensIndex(index), false, values))}
              label={$_('energiatodistus.haku.false')} />
          </div>
        {/if}
      </div>
    {/each}
  </div>
</div>
