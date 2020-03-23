<script>
  import * as R from 'ramda';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as ToimintaAlueUtils from '@Component/Geo/toimintaalue-utils';

  import Checkbox from '@Component/Checkbox/Checkbox';

  export let toimintaalueet = [];
  export let mainToimintaalue = 0;
  export let selected = [];
  export let limit = 5;

  const mergeSelected = right =>
    R.compose(
      R.mergeRight(right),
      ToimintaAlueUtils.formatSelected
    )(selected);

  let allSelected = R.compose(
    R.assoc(mainToimintaalue, true),
    mergeSelected,
    ToimintaAlueUtils.toimintaAlueetToSelect
  )(toimintaalueet);

  $: selected = R.compose(
    R.reject(R.equals(mainToimintaalue)),
    R.map(
      R.compose(
        parseInt,
        R.head
      )
    ),
    R.filter(R.last),
    R.toPairs
  )(allSelected);

  $: currentLimit = limit - R.length(selected);
</script>

<style type="text/postcss">
  ol {
    @apply flex flex-row flex-wrap;
  }

  li {
    @apply flex-grow w-1/3 mb-6;
  }
</style>

<ol>
  {#each R.sortBy(R.prop(`label-${LocaleUtils.shortLocale($locale)}`), toimintaalueet) as toimintaalue}
    <li>
      <Checkbox
        label={LocaleUtils.label($locale, toimintaalue)}
        bind:model={allSelected}
        lens={R.lensProp(toimintaalue.id)}
        disabled={toimintaalue.id === mainToimintaalue || (currentLimit <= 0 && !R.includes(toimintaalue.id, selected))} />
    </li>
  {/each}
</ol>
