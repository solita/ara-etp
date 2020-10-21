<script>
  import { slide } from 'svelte/transition';
  import { querystring, location, push } from 'svelte-spa-router';
  import qs from 'qs';
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
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

  const valittuHaku = Maybe.None();

  const navigate = search => {
    R.compose(
      push,
      R.concat(`${$location}?`),
      params => qs.stringify(params, { encode: false }),
      R.mergeRight(qs.parse($querystring)),
      R.assoc('where', R.__, {})
    )(search);
  };

  $: showHakukriteerit = where.length > 0;

  let queryItems = [];

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
  <div class="w-1/12" />
  <div class="flex justify-center items-center w-1/3 pl-4">
    <div class="w-full">
      <Select
        allowNone={true}
        label={$_('energiatodistus.haku.suosikkihaut')}
        model={valittuHaku}
        lens={a => a} />
    </div>
  </div>
</div>

<div class="flex w-full justify-end mt-8">
  <ToggleButton
    label={$_('energiatodistus.haku.lisaa_hakuehtoja')}
    bind:value={showHakukriteerit} />
</div>

<form
  novalidate
  on:submit|preventDefault={evt => {
    const parsedHakuForm = EtHakuUtils.parseHakuForm(evt.target);
    R.compose( navigate, EtHakuUtils.searchString, EtHakuUtils.searchItems )(parsedHakuForm);
  }}
  on:reset|preventDefault={_ => {
    queryItems = [];
  }}>

  {#each queryItems as { conjunction, block: [operator, key, ...values] }, index}
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
        on:click={_ => (queryItems = EtHakuUtils.removeQueryItem(index, queryItems))}>
        delete
      </span>
    </div>
  {/each}

  <div class="flex">
    <TextButton
      text={$_('energiatodistus.haku.lisaa_hakuehto')}
      icon={'add_circle_outline'}
      type={'button'}
      on:click={_ => {
        queryItems = R.append(EtHakuUtils.defaultQueryItem(), queryItems);
      }} />
  </div>

  <div class="flex">
    <div class="w-1/5">
      <Button type="submit" text={$_('energiatodistus.haku.hae')} />
    </div>
    <div class="w-1/3">
      <Button
        type="reset"
        style="secondary"
        text={$_('energiatodistus.haku.tyhjenna_hakuehdot')} />
    </div>
  </div>
</form>
