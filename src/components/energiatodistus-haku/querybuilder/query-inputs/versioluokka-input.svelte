<script>
  import SimpleInput from '@Component/Input/SimpleInput';
  import * as parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as R from 'ramda';
  import { _, locale } from '@Language/i18n';

  import VersioInput from './versio-input';
  import Select from '@Component/Select/Select';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';

  export let values;
  export let nameprefix;
  export let luokittelut;
  export let key = 'kayttotarkoitusluokat';

  let [versiovalue, luokitteluvalue] = values;

  let currentluokittelut = Maybe.None();

  $: currentluokittelut = Maybe.fromNull(
    R.path([versiovalue, key], luokittelut)
  );

  $: console.log(currentluokittelut);

  $: labelLocale = LocaleUtils.label($locale);

  $: if (
    R.compose(
      Maybe.isNone,
      R.filter(R.includes(luokitteluvalue)),
      R.map(R.pluck('id'))
    )(currentluokittelut)
  ) {
    console.log('asdf');
    luokitteluvalue = R.compose(
      Maybe.orSome(1),
      R.map(
        R.compose(
          R.prop('id'),
          R.head
        )
      )
    )(currentluokittelut);
  }
</script>

<div class="w-1/4">
  <VersioInput bind:value={versiovalue} {nameprefix} />
</div>

{#if currentluokittelut}
  <div class="flex-grow ml-2">
    <Select
      name={`${nameprefix}_value_1`}
      allowNone={false}
      model={luokitteluvalue || R.compose( Maybe.orSome(1), R.map(R.prop('id')), R.head )(currentluokittelut)}
      items={R.compose( Maybe.orSome([]), R.map(R.pluck('id')) )(currentluokittelut)}
      format={EtUtils.selectFormat(labelLocale, Maybe.orSome([], currentluokittelut))}
      lens={R.identity} />
  </div>
{/if}
