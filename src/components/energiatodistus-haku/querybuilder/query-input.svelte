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
    }

    return value;
  });

  let operation;
  let values;

  $: operation = R.compose(
    EtHakuUtils.operationFromBlock(operations),
    R.view(lens)
  )(model);

  $: values = R.compose(
    R.map(parseByOperationType(operation)),
    R.take(operation.argumentNumber),
    R.unless(
      R.gt(operation.argumentNumber),
      R.concat(R.__, operation.defaultValues())
    ),
    R.drop(2),
    R.view(lens)
  )(model);

  $: updateModel(operation, values);
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
    {#each values as value, index}
      <div class="flex flex-col justify-end">
        <SimpleInput
          center={true}
          on:input={evt => (values = R.set(R.lensIndex(index), parseByOperationType(operation, evt.target.value), values))}
          viewValue={value} />
      </div>
    {/each}
  </div>
</div>
