<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';

  import NumberOperatorInput from '@Component/energiatodistus-haku/querybuilder/query-inputs/number-operator-input';
  import OperatorInput from '@Component/energiatodistus-haku/querybuilder/query-inputs/operator-input';
  import BooleanInput from '@Component/energiatodistus-haku/querybuilder/query-inputs/boolean-input';
  import DateInput from '@Component/energiatodistus-haku/querybuilder/query-inputs/date-input';

  import QueryInput from '@Component/energiatodistus-haku/querybuilder/query-input';

  export let model;
  export let lens;

  export let schema;

  const mapOperatorTypeToComponent = type =>
    R.prop(type, {
      [EtHakuUtils.OPERATOR_TYPES.STRING]: OperatorInput,
      [EtHakuUtils.OPERATOR_TYPES.NUMBER]: NumberOperatorInput,
      [EtHakuUtils.OPERATOR_TYPES.DATESINGLE]: DateInput,
      [EtHakuUtils.OPERATOR_TYPES.BOOLEAN]: BooleanInput
    });

  const conjunctionLens = R.compose(
    lens,
    R.lensProp('conjunction')
  );

  const blockLens = R.compose(
    lens,
    R.lensProp('block')
  );

  const operationLens = R.compose(
    blockLens,
    R.lensIndex(0)
  );

  const keyLens = R.compose(
    blockLens,
    R.lensIndex(1)
  );

  let conjunction = R.view(conjunctionLens, model);

  let key = R.view(keyLens, model);

  $: model = R.set(conjunctionLens, conjunction, model);

  $: operations = R.prop(key, schema);
</script>

<div class="w-full flex flex-col">
  {#if Maybe.isSome(conjunction)}
    <div class="flex justify-between w-1/6 my-10">
      <Radio
        bind:group={conjunction}
        value={Maybe.Some('and')}
        label={$_('energiatodistus.haku.and')}
        on:click={evt => (conjunction = Maybe.Some('and'))} />
      <Radio
        bind:group={conjunction}
        value={Maybe.Some('or')}
        label={$_('energiatodistus.haku.or')}
        on:click={evt => (conjunction = Maybe.Some('or'))} />
    </div>
  {/if}

  <div class="flex-grow flex items-center justify-start">
    <div class="w-1/2 mr-4">
      <Select
        items={R.keys(schema)}
        bind:model={key}
        format={R.compose( $_, R.concat('energiatodistus.haku.') )}
        parse={R.identity}
        lens={R.identity}
        allowNone={false} />
    </div>
    <div class="w-1/2">
      <QueryInput bind:model lens={blockLens} {operations} />
    </div>
  </div>
</div>
