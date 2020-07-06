<script>
  import { slide } from 'svelte/transition';
  import { flip } from 'svelte/animate';

  import qs from 'qs';
  import { v4 as uuidv4 } from 'uuid';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Button from '@Component/Button/Button';

  import QueryBlock from './queryblock';

  export let where;
  export let navigate;

  console.log(where);

  const firstConjunctionLens = R.compose(
    R.lensIndex(0),
    R.lensProp('conjunction')
  );

  $: parsedWhere = R.compose(
    R.map(([conjunction, block]) => ({ conjunction, block, uuid: uuidv4() })),
    R.over(R.lensIndex(0), R.pair(Maybe.None())),
    R.unnest,
    R.converge(R.concat, [
      R.take(1),
      R.compose(
        R.map(R.over(R.lensIndex(0), R.pair(Maybe.Some('or')))),
        R.tail
      )
    ]),
    R.map(
      R.converge(R.concat, [
        R.take(1),
        R.compose(
          R.map(R.pair(Maybe.Some('and'))),
          R.tail
        )
      ])
    )
  )(where);

  $: formatedWhere = R.compose(
    JSON.stringify,
    R.reduce(
      (acc, { conjunction, block }) => {
        if (Maybe.exists(R.equals('or'), conjunction)) {
          return R.append([block], acc);
        }

        const lastLens = R.lensIndex(R.length(acc) - 1);

        return R.over(lastLens, R.append(block), acc);
      },
      [[]]
    )
  )(parsedWhere);
</script>

<style>
  /* your styles go here */
</style>

<div class="flex flex-col w-full">
  {#each parsedWhere as { conjunction, block, uuid }, index (uuid)}
    <div
      transition:slide|local={{ duration: 200 }}
      animate:flip={{ duration: 200 }}>
      <QueryBlock bind:model={parsedWhere} lens={R.lensIndex(index)} />
      <span
        on:click={_ => (parsedWhere = R.compose( R.tap(console.log), R.set(firstConjunctionLens, Maybe.None()), R.tap(console.log), R.remove(index, 1) )(parsedWhere))}>
        X
      </span>
    </div>
  {/each}

  <Button text={'Hae'} on:click={() => navigate(formatedWhere)} />
</div>
