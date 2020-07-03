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
  import QueryBuilder from './querybuilder/querybuilder';

  import { _ } from '@Language/i18n';

  const hakuKriteerit = EtHakuUtils.perustiedot();

  let valitutKriteerit = [];

  let hakuValue = '';

  const valittuHaku = Maybe.None();

  let where = R.compose(
    Maybe.orSome([]),
    R.map(JSON.parse),
    Maybe.fromNull,
    R.prop('where'),
    qs.parse
  )($querystring);

  let showHakukriteerit = where.length > 0;
</script>

<style>

</style>

<div class="flex w-full">
  <div class="w-7/12">
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
  <ToggleButton label={'Lisää hakuehtoja'} bind:value={showHakukriteerit} />
</div>

{#if showHakukriteerit}
  <div transition:slide|local={{ duration: 200 }} class="flex w-full">
    <QueryBuilder bind:where />
  </div>
{/if}
