<script>
  import qs from 'qs';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';
  import { _ } from '@Language/i18n';

  import * as EtHakuUtils from '@Component/Energiatodistus/energiatodistus-haku-utils';

  import QueryBlock from './queryblock';

  export let where;
  export let navigate;

  if (!R.length(R.flatten(where))) {
    where = [[EtHakuUtils.defaultKriteeri()]];
  }

  const firstConjunctionLens = R.compose(
    R.lensIndex(0),
    R.lensProp('conjunction')
  );

  $: parsedWhere = R.compose(
    R.map(([conjunction, block]) => ({ conjunction, block })),
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

  const formatWhere = R.compose(
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
  );
</script>

<div class="flex flex-col w-full">
  {#each parsedWhere as { conjunction, block }, index}
    <div class="flex justify-start items-end">
      <QueryBlock bind:model={parsedWhere} lens={R.lensIndex(index)} />
      <span
        class="text-primary font-icon text-2xl cursor-pointer ml-4
        hover:text-secondary"
        on:click={_ => (where = R.compose( formatWhere, R.set(firstConjunctionLens, Maybe.None()), R.remove(index, 1) )(parsedWhere))}>
        delete
      </span>
    </div>
  {/each}
  <div>
    <TextButton
      text={$_('energiatodistus.haku.lisaa_hakuehto')}
      icon={'add_circle_outline'}
      on:click={evt => (where = R.compose( formatWhere, R.append({
            conjunction: Maybe.Some('and'),
            block: EtHakuUtils.defaultKriteeri()
          }) )(parsedWhere))} />
  </div>
  <div class="flex">
    <div class="w-1/5">
      <Button
        text={$_('energiatodistus.haku.hae')}
        on:click={() => navigate(formatWhere(parsedWhere))} />
    </div>
    <div class="w-1/3">
      <Button
        text={$_('energiatodistus.haku.tyhjenna_hakuehdot')}
        style={'secondary'}
        on:click={evt => (where = EtHakuUtils.defaultKriteeri())} />
    </div>
  </div>
</div>
