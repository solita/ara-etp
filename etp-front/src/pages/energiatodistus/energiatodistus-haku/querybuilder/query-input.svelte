<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { OPERATOR_TYPES } from '@Pages/energiatodistus/energiatodistus-haku/schema';

  import Select from '@Component/Select/Select';

  import BooleanInput from './query-inputs/boolean-input';
  import TextInput from './query-inputs/text-input';
  import DateInput from './query-inputs/date-input';
  import DateBetweenInput from './query-inputs/date-between-input';
  import DayCountInput from './query-inputs/day-count-input';
  import NumberInput from './query-inputs/number-input';
  import UnformattedNumberInput from './query-inputs/unformatted-number-input';
  import PercentInput from './query-inputs/percent-input';
  import VersioInput from './query-inputs/versio-input';
  import KayttotarkoitusInput from './query-inputs/kayttotarkoitus-input';
  import AlakayttotarkoitusInput from './query-inputs/alakayttotarkoitus-input';
  import EluokkaInput from './query-inputs/e-luokka-input';
  import TilaInput from './query-inputs/tila-input';
  import LaatijaInput from './query-inputs/laatija-input';
  import KuntaInput from './query-inputs/kunta-input';
  import LaatimisvaiheInput from './query-inputs/laatimisvaihe-input';
  import KielisyysInput from './query-inputs/kielisyys-input';
  import PatevyystasoInput from './query-inputs/patevyystaso-input';
  import IlmanvaihtotyyppiInput from './query-inputs/ilmanvaihtotyyppi-input';
  import LammitysMuotoInput from './query-inputs/lammitysmuoto-input';

  import { _ } from '@Language/i18n';

  export let nameprefix = '';

  export let operations;
  export let operation;
  export let values;
  export let luokittelut;
  export let laatijat;
  export let kunnat;

  const inputForType = type => {
    switch (type) {
      case OPERATOR_TYPES.DAYCOUNT:
        return DayCountInput;
      case OPERATOR_TYPES.NUMBER:
        return NumberInput;
      case OPERATOR_TYPES.UNFORMATTED_NUMBER:
        return UnformattedNumberInput;
      case OPERATOR_TYPES.PERCENT:
        return PercentInput;
      case OPERATOR_TYPES.BOOLEAN:
        return BooleanInput;
      case OPERATOR_TYPES.DATE:
        return DateInput;
      case OPERATOR_TYPES.DATE_BETWEEN:
        return DateBetweenInput;
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
      case OPERATOR_TYPES.LAATIJA:
        return LaatijaInput;
      case OPERATOR_TYPES.LAATIMISVAIHE:
        return LaatimisvaiheInput;
      case OPERATOR_TYPES.KIELISYYS:
        return KielisyysInput;
      case OPERATOR_TYPES.ILMANVAIHTOTYYPPI:
        return IlmanvaihtotyyppiInput;
      case OPERATOR_TYPES.PATEVYYSTASO:
        return PatevyystasoInput;
      case OPERATOR_TYPES.KUNTA:
        return KuntaInput;
      case OPERATOR_TYPES.LAMMITYSMUOTO:
        return LammitysMuotoInput;
      default:
        return TextInput;
    }
  };

  let op = R.compose(
    Maybe.orSome(R.head(operations)),
    Maybe.fromNull,
    R.find(R.pathEq(operation, ['operation', 'browserCommand']))
  )(operations);
</script>

<style type="text/postcss">
  .inputs > div:not(:first-child) {
    @apply ml-2;
  }
</style>

<div class="flex">
  <input
    class="hidden"
    tabindex="-1"
    name={`${nameprefix}_type`}
    value={op.type} />
  {#if op.type !== OPERATOR_TYPES.BOOLEAN}
    <div class="flex items-end w-1/4">
      <div class="flex-grow">
        <Select
          items={operations}
          bind:model={op}
          format={R.compose(
            $_,
            R.concat('energiatodistus.haku.'),
            R.path(['operation', 'browserCommand'])
          )}
          inputValueParse={R.path(['operation', 'browserCommand'])}
          lens={R.identity}
          allowNone={false}
          name={`${nameprefix}_operation`} />
      </div>
    </div>
  {:else}
    <input
      class="hidden"
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
        {luokittelut}
        {laatijat}
        {kunnat} />
    </div>
  </div>
</div>
