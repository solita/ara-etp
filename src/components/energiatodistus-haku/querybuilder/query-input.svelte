<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';
  import { OPERATOR_TYPES } from '@Component/energiatodistus-haku/schema';

  import Select from '@Component/Select/Select';
  import SimpleInput from '@Component/Input/SimpleInput';
  import { _ } from '@Language/i18n';

  export let operations;
  export let model;
  export let lens;

  const operationLens = R.compose(
    lens,
    R.lensIndex(0)
  );

  const inputFromOperation = R.curry((operation, input, valuesFromBlock) => {
    const newInput = { type: operation.type };

    if (R.equals(input.type, operation.type)) {
      return R.assoc('values', valuesFromBlock, newInput);
    }

    return R.assoc('values', operation.defaultValues, newInput);
  });

  const updateModel = R.curry((model, operation, input) =>
    R.compose(
      R.set(lens, R.__, model),
      EtHakuUtils.blockFromOperation(operation),
      R.prop('values')
    )(input)
  );

  let block;
  let operation;

  let input = { type: OPERATOR_TYPES.NUMBER, values: [] };

  $: {
    block = R.view(lens, model);
    operation = EtHakuUtils.operationFromBlock(operations, block);
    input = R.compose(
      inputFromOperation(operation, input),
      EtHakuUtils.valuesFromBlock
    )(operation, block);

    model = updateModel(model, operation, input);
  }

  $: console.log(model);
</script>

<style type="text/postcss">
  .inputs > div:not(:first-child) {
    @apply ml-2;
  }
</style>

<div class="flex">
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
  <div class="inputs w-1/2 pl-4 flex justify-between">
    {#each input.values as value, index}
      <div class="flex flex-col justify-end">
        <SimpleInput
          center={true}
          on:input={evt => (model = R.compose( updateModel(model, operation), R.set(R.compose( R.lensProp('values'), R.lensIndex(index) ), evt.target.value) )(input))}
          viewValue={value} />
      </div>
    {/each}
  </div>
</div>
