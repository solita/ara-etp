<script>
  import * as R from 'ramda';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';

  import Checkbox from '@Component/Checkbox/Checkbox';

  export let toimintaalueet = [];
  export let mainToimintaalue = 0;
  export let selected = {};

  const mergeSelected = right => R.mergeLeft(selected, right);

  let allSelected = R.converge(
    R.compose(
      R.assoc(mainToimintaalue, true),
      mergeSelected,
      R.zipObj
    ),
    [
      R.map(R.prop('id')),
      R.compose(
        R.repeat(false),
        R.inc,
        R.reduce(R.max, 0),
        R.pluck('id')
      )
    ]
  )(toimintaalueet);

  $: console.log(allSelected);
</script>

<style>

</style>

<ol>
  {#each toimintaalueet as toimintaalue}
    <li>
      <Checkbox
        label={LocaleUtils.label($locale, toimintaalue)}
        bind:model={allSelected}
        lens={R.lensProp(toimintaalue.id)}
        disabled={toimintaalue.id === mainToimintaalue} />
    </li>
  {/each}
</ol>
