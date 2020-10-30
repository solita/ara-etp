<script>
  import { tick } from 'svelte';
  import { slide } from 'svelte/transition';
  import { querystring, location, push } from 'svelte-spa-router';
  import qs from 'qs';
  import * as R from 'ramda';

  import { flashMessageStore } from '@/stores';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EtHakuUtils from './energiatodistus-haku-utils';
  import SimpleInput from '@Component/Input/SimpleInput';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import Select from '@Component/Select/Select';
  import ToggleButton from '@Component/ToggleButton/ToggleButton';
  import QueryBlock from './querybuilder/queryblock';
  import Button from '@Component/Button/Button';
  import TextButton from '@Component/Button/TextButton';
  import Radio from '@Component/Radio/Radio';

  import { _ } from '@Language/i18n';

  export let where;
  export let schema;

  let hakuValue = '';

  const navigate = search => {
    R.compose(
      push,
      R.concat(`${$location}?`),
      params => qs.stringify(params, { encode: false }),
      R.mergeRight(qs.parse($querystring)),
      R.assoc('where', R.__, {})
    )(search);
  };

  let queryItems = [];

  let form;

  let valid = true;

  $: queryItems = R.ifElse(
    R.length,
    EtHakuUtils.deserializeWhere(schema),
    R.always([])
  )(where);
</script>

<style>
  .conjunction:first-of-type {
    display: none;
  }
</style>

<div class="flex w-full">
  <div class="w-7/12 flex flex-col justify-end">
    <SimpleInput
      label={' '}
      wrapper={PillInputWrapper}
      rawValueAsViewValue={true}
      search={true}
      bind:rawValue={hakuValue} />
  </div>
</div>

<form
  bind:this={form}
  on:change={evt => {
    valid = R.compose( Either.orSome(false), R.map(R.compose( R.all(EtHakuUtils.isValidBlock(schema)), R.map(R.prop('block')), EtHakuUtils.searchItems )), f => Either.fromTry(
          () => EtHakuUtils.parseHakuForm(f)
        ) )(form);
  }}
  novalidate
  on:submit|preventDefault={evt => {
    R.compose( R.chain(navigate), R.map(EtHakuUtils.searchString), R.map(EtHakuUtils.searchItems), evt => Either.fromTry(
          () => EtHakuUtils.parseHakuForm(evt.target)
        ) )(evt);
  }}
  on:reset|preventDefault={_ => {
    queryItems = [];
    valid = true;
  }}>

  {#each queryItems as { conjunction, block: [operator, key, ...values] }, index (`${index}_${operator}_${key}_${R.join('_', values)}`)}
    <div class="conjunction w-full flex flex-col">
      <div class="flex justify-between w-1/6 my-10">
        <Radio
          group={conjunction}
          value={'and'}
          label={$_('energiatodistus.haku.and')}
          name={`${index}_conjunction`}
          on:click={_ => {
            queryItems = R.over(R.lensIndex(index), R.assoc('conjunction', 'and'), queryItems);
          }} />
        <Radio
          group={conjunction}
          value={'or'}
          label={$_('energiatodistus.haku.or')}
          name={`${index}_conjunction`}
          on:click={_ => {
            queryItems = R.over(R.lensIndex(index), R.assoc('conjunction', 'or'), queryItems);
          }} />
      </div>
    </div>
    <div class="flex justify-start items-end">
      <QueryBlock {operator} {key} {values} {index} {schema} />
      <span
        class="text-primary font-icon text-2xl cursor-pointer ml-4
        hover:text-secondary"
        on:click={async _ => {
          const newItems = R.compose( R.map(EtHakuUtils.removeQueryItem(index)), R.map(EtHakuUtils.searchItems) )(Either.fromTry(
              () => EtHakuUtils.parseHakuForm(form)
            ));
          if (Either.isRight(newItems)) {
            queryItems = Either.right(newItems);
            await tick();
            form.dispatchEvent(new Event('change'));
          } else {
            flashMessageStore.add('energiatodistus', 'warn', 'Hakukriteerin poistamisessa tapahtui virhe.');
          }
        }}>
        delete
      </span>
    </div>
  {/each}

  <div class="flex my-4">
    <TextButton
      text={$_('energiatodistus.haku.lisaa_hakuehto')}
      icon={'add_circle_outline'}
      type={'button'}
      on:click={async _ => {
        const newItems = R.compose( R.map(R.append(EtHakuUtils.defaultQueryItem())), R.map(EtHakuUtils.searchItems) )(Either.fromTry(
            () => EtHakuUtils.parseHakuForm(form)
          ));
        if (Either.isRight(newItems)) {
          queryItems = Either.right(newItems);
          await tick();
          form.dispatchEvent(new Event('change'));
        } else {
          flashMessageStore.add('energiatodistus', 'warn', 'Hakukriteerin lisäyksessä tapahtui virhe.');
        }
      }} />
  </div>

  <div class="flex">
    <div class="w-1/5">
      <Button
        disabled={!valid}
        type="submit"
        text={$_('energiatodistus.haku.hae')} />
    </div>
    <div class="w-1/3">
      <Button
        type="reset"
        style="secondary"
        text={$_('energiatodistus.haku.tyhjenna_hakuehdot')} />
    </div>
  </div>
</form>
