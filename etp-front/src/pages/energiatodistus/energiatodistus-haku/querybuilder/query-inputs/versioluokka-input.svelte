<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as R from 'ramda';
  import { locale } from '@Language/i18n';

  import Select from '@Component/Select/Select';

  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as LocaleUtils from '@Language/locale-utils';
  import { isEtp2026Enabled } from '@Utility/config_utils.js';

  export let config;
  export let luokittelut;
  export let key = 'alakayttotarkoitusluokat';
  export let versio;
  export let luokittelu;

  const versiot = isEtp2026Enabled(config) ? [2026, 2018, 2013] : [2018, 2013];

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
      items={versiot}
      lens={R.identity} />
  </div>

  {#if currentluokittelut}
    <div class="ml-2 flex-grow">
      <Select
        allowNone={false}
        bind:model={luokittelu}
        items={R.compose(
          Maybe.orSome([]),
          R.map(R.pluck('id'))
        )(currentluokittelut)}
        format={EtUtils.selectFormat(
          labelLocale,
          Maybe.orSome([], currentluokittelut)
        )}
        lens={R.identity} />
    </div>
  {/if}
</div>
