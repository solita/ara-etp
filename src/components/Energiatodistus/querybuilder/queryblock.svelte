<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Radio from '@Component/Radio/Radio';
  import Select from '@Component/Select/Select';
  import { _ } from '@Language/i18n';

  import * as EtHakuUtils from '@Component/Energiatodistus/energiatodistus-haku-utils';

  export let model;
  export let lens;

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

  const findKriteeri = R.curry((kriteerit, key) =>
    R.compose(
      Maybe.fromNull,
      R.find(
        R.compose(
          R.equals(key),
          R.prop('key')
        )
      )
    )(kriteerit)
  );

  let conjunction = R.view(conjunctionLens, model);

  let key = Maybe.fromNull(R.view(keyLens, model));

  let kriteeri = R.compose(
    R.assoc('kriteeri', R.__, {}),
    R.chain(findKriteeri(laatijaKriteerit)),
    Maybe.fromNull,
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
            R.compose(
              R.split(' '),
              k.defaultOperator.command
            )(k.defaultValue),
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
          this={k.component}
          bind:model
          operators={k.operators}
          lens={blockLens} />
      {/each}
    </div>
  </div>
</div>
