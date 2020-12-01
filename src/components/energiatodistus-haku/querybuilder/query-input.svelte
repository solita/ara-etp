<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { OPERATOR_TYPES } from '@Component/energiatodistus-haku/schema';

  import Select from '@Component/Select/Select';

  import BooleanInput from './query-inputs/boolean-input';
  import TextInput from './query-inputs/text-input';
  import DateInput from './query-inputs/date-input';
  import NumberInput from './query-inputs/number-input';
  import VersioInput from './query-inputs/versio-input';
  import VersioluokkaInput from './query-inputs/versioluokka-input';
  import KayttotarkoitusInput from './query-inputs/kayttotarkoitus-input';
  import AlakayttotarkoitusInput from './query-inputs/alakayttotarkoitus-input';
  import EluokkaInput from './query-inputs/e-luokka-input';
  import TilaInput from './query-inputs/tila-input';

  import { _ } from '@Language/i18n';

  export let nameprefix = '';

  export let operations;
  export let operation;
  export let values;
  export let luokittelut;

  const inputForType = type => {
    switch (type) {
      case OPERATOR_TYPES.NUMBER:
        return NumberInput;
      case OPERATOR_TYPES.BOOLEAN:
        return BooleanInput;
      case OPERATOR_TYPES.DATE:
        return DateInput;
      case OPERATOR_TYPES.VERSIO:
        return VersioInput;
      case OPERATOR_TYPES.ELUOKKA:
        return EluokkaInput;
      case OPERATOR_TYPES.VERSIOLUOKKA:
        return AlakayttotarkoitusInput;
      case OPERATOR_TYPES.TILA:
        return TilaInput;
      case OPERATOR_TYPES.VERSIOKAYTTOTARKOITUSLUOKKA:
        return KayttotarkoitusInput;
      default:
        return TextInput;
    }
  };

  $: op = R.compose(
    Maybe.orSome(R.head(operations)),
    Maybe.fromNull,
    R.find(R.pathEq(['operation', 'browserCommand'], operation))
  )(operations);
</script>

<style type="text/postcss">
  .inputs > div:not(:first-child) {
    @apply ml-2;
  }
</style>

<div class="flex">
  <input
    class="sr-only"
    tabindex="-1"
    name={`${nameprefix}_type`}
    value={op.type} />
  {#if op.type !== OPERATOR_TYPES.BOOLEAN}
    <div class="flex items-end w-1/4">
      <div class="flex-grow">
        <Select
          items={R.map(R.path(['operation', 'browserCommand']), operations)}
          model={R.path(['operation', 'browserCommand'], op)}
          lens={R.identity}
          format={R.compose($_, R.concat('energiatodistus.haku.'))}
          allowNone={false}
          name={`${nameprefix}_operation`} />
      </div>
    </div>
  {:else}
    <input
      class="sr-only"
      tabindex="-1"
      name={`${nameprefix}_operation`}
      value="=" />
  {/if}
  <div class="inputs pl-4 flex justify-between flex-grow flex-row items-end">
    <div class="w-full">
      <svelte:component
        this={inputForType(op.type)}
        {values}
        {nameprefix}
        {luokittelut} />
    </div>
  </div>
</div>
