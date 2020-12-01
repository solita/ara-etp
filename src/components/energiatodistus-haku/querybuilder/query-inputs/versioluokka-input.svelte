<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as R from 'ramda';
  import { _, locale } from '@Language/i18n';

  import VersioInput from './versio-input';
  import Select from '@Component/Select/Select';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';

  export let luokittelut;
  export let key = 'alakayttotarkoitusluokat';
  export let versio;
  export let luokittelu;

  let currentluokittelut = Maybe.None();

  $: currentluokittelut = Maybe.fromNull(R.path([versio, key], luokittelut));

  $: labelLocale = LocaleUtils.label($locale);

  $: if (
    R.compose(
      Maybe.isNone,
      R.filter(R.includes(luokittelu)),
      R.map(R.pluck('id'))
    )(currentluokittelut)
  ) {
    luokittelu = R.compose(
      Maybe.orSome(1),
      R.map(R.compose(R.prop('id'), R.head))
    )(currentluokittelut);
  }
</script>

<div class="flex flex-row items-end">
  <div class="w-1/4">
    <Select
      allowNone={false}
      bind:model={versio}
      items={[2018, 2013]}
      lens={R.identity} />
  </div>

  {#if currentluokittelut}
    <div class="flex-grow ml-2">
      <Select
        allowNone={false}
        model={luokittelu || R.compose(Maybe.orSome(1), R.map(R.prop('id')), R.head)(currentluokittelut)}
        items={R.compose(Maybe.orSome([]), R.map(R.pluck('id')))(currentluokittelut)}
        format={EtUtils.selectFormat(labelLocale, Maybe.orSome([], currentluokittelut))}
        lens={R.identity}
        on:change />
    </div>
  {/if}
</div>
