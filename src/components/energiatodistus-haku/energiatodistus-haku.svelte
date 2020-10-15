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
  import QueryBuilder from './querybuilder/qb';
  import QueryRunner from './querybuilder/queryrunner';
  import Button from '@Component/Button/Button';

  import { _ } from '@Language/i18n';

  export let parsedQuery;
  export let runQuery;
  export let schema;

  let where = R.prop('where', parsedQuery);

  let hakuValue = '';

  const valittuHaku = Maybe.None();

  const navigate = search => {
    R.compose(
      push,
      R.tap(console.log),
      R.concat(`${$location}?`),
      params => qs.stringify(params, { encode: false }),
      R.mergeRight(qs.parse($querystring)),
      R.assoc('where', R.__, {})
    )(search);
  };

  $: showHakukriteerit = where.length > 0;

  $: queryItems = R.ifElse(
    R.length,
    EtHakuUtils.deserializeWhere,
    R.always([])
  )(where);

  $: console.log(parsedQuery);
</script>

<style>

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
    R.compose( navigate, EtHakuUtils.searchString, EtHakuUtils.parseHakuForm )(evt.target);
  }}
  on:reset|preventDefault={evt => {
    debugger;
  }}>

  {#each queryItems as { conjunction, block: [operator, key, ...values] }, index}
    <QueryBuilder {conjunction} {operator} {key} {values} {index} {schema} />
  {/each}

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

<!--
{#if showHakukriteerit}
  <div transition:slide|local={{ duration: 200 }} class="flex w-full">
    <QueryBuilder bind:where {navigate} {schema} />
  </div>
{/if}
-->
