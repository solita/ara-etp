<script>
  import SimpleInput from '@Component/Input/SimpleInput';
  import * as parsers from '@Utility/parsers';
  import * as Either from '@Utility/either-utils';
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

  $: currentluokittelut = R.path([versiovalue, key], luokittelut);
  $: labelLocale = LocaleUtils.label($locale);
</script>

<div class="w-1/4">
  <VersioInput bind:value={versiovalue} {nameprefix} />
</div>

{#if currentluokittelut}
  <div class="flex-grow ml-2">
    <Select
      name={`${nameprefix}_value_1`}
      allowNone={false}
      model={luokitteluvalue || R.prop('id', R.head(currentluokittelut))}
      items={R.pluck('id', currentluokittelut)}
      format={EtUtils.selectFormat(labelLocale, currentluokittelut)}
      lens={R.identity} />
  </div>
{/if}
