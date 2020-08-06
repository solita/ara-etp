<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  import * as EtHakuUtils from '@Component/Energiatodistus/energiatodistus-haku-utils';

  import NumberOperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/number-operator-input';
  import OperatorInput from '@Component/Energiatodistus/querybuilder/query-inputs/operator-input';
  import BooleanInput from '@Component/Energiatodistus/querybuilder/query-inputs/boolean-input';
  import DateInput from '@Component/Energiatodistus/querybuilder/query-inputs/date-input';

  export let model;
  export let lens;

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

  const keyLens = R.compose(
    blockLens,
    R.lensIndex(1)
  );

  const laatijaKriteerit = EtHakuUtils.laatijaKriteerit();

  let conjunction = R.view(conjunctionLens, model);

  let kriteeri = R.compose(
    R.assoc('kriteeri', R.__, {}),
    EtHakuUtils.findFromKriteeritByKey(laatijaKriteerit),
    R.view(keyLens)
  )(model);

  $: model = R.set(conjunctionLens, conjunction, model);

  $: R.compose(
    R.forEach(
      R.unless(
        R.compose(
          R.equals(R.view(keyLens, model)),
          R.prop('key')
        ),
        k =>
          (model = R.set(
            blockLens,
            k.defaultOperator.command(...k.defaultValues),
            model
          ))
      )
    ),

    R.prop('kriteeri')
  )(kriteeri);

  $: R.compose(
    Maybe.orElseRun(() => (model = R.set(blockLens, [], model))),
    R.prop('kriteeri')
  )(kriteeri);
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
        items={laatijaKriteerit}
        bind:model={kriteeri}
        format={R.compose( $_, R.concat('energiatodistus.haku.'), R.prop('key') )}
        parse={Maybe.Some}
        lens={R.lensProp('kriteeri')}
        allowNone={false} />
    </div>
    <div class="w-1/2">
      {#each R.compose( Maybe.toArray, R.prop('kriteeri') )(kriteeri) as k}
        <svelte:component
          this={mapOperatorTypeToComponent(k.type)}
          bind:model
          operators={k.operators}
          lens={blockLens} />
      {/each}
    </div>
  </div>
</div>
