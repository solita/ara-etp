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

  let queryItems = EtHakuUtils.deserializeWhere(where);

  if (!R.length(queryItems)) {
    queryItems = [EtHakuUtils.defaultKriteeriQueryItem()];
  }
</script>

<div class="flex flex-col w-full">
  {#each queryItems as { conjunction, block }, index}
    <div class="flex justify-start items-end">
      <QueryBlock bind:model={queryItems} lens={R.lensIndex(index)} />
      <span
        class="text-primary font-icon text-2xl cursor-pointer ml-4
        hover:text-secondary"
        on:click={_ => (queryItems = EtHakuUtils.removeQueryItem(index, queryItems))}>
        delete
      </span>
    </div>
  {/each}
  <div>
    <TextButton
      text={$_('energiatodistus.haku.lisaa_hakuehto')}
      icon={'add_circle_outline'}
      on:click={evt => {
        const items = EtHakuUtils.appendDefaultQueryItem(queryItems);
        queryItems = R.when(R.compose( R.equals(1), R.length, R.tap(console.log) ), R.set(R.compose( R.lensIndex(0), R.lensProp('conjunction') ), Maybe.None()), items);
      }} />
  </div>
  <div class="flex">
    <div class="w-1/5">
      <Button
        text={$_('energiatodistus.haku.hae')}
        on:click={() => navigate(EtHakuUtils.serializeWhere(queryItems))} />
    </div>
    <div class="w-1/3">
      <Button
        text={$_('energiatodistus.haku.tyhjenna_hakuehdot')}
        style={'secondary'}
        on:click={evt => (queryItems = [])} />
    </div>
  </div>
</div>
