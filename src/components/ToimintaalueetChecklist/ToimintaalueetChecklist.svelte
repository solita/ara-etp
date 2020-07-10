<script>
  import * as R from 'ramda';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as ToimintaAlueUtils from '@Component/Geo/toimintaalue-utils';

  import * as Maybe from '@Utility/maybe-utils';

  import Checkbox from '@Component/Checkbox/Checkbox';

  export let toimintaalueet = [];
  export let limit = 5;

  export let label = '';

  export let model;
  export let lens;

  export let format = R.identity;
  export let disabled = false;

  $: mainToimintaalue = model.toimintaalue;

  $: selected = ToimintaAlueUtils.toimintaalueetWithoutMain(
    mainToimintaalue,
    R.view(lens, model)
  );

  $: toimintaalueLens = toimintaalue =>
    R.lens(
      R.compose(
        R.or(
          ToimintaAlueUtils.isMainToimintaAlue(mainToimintaalue, toimintaalue)
        ),
        R.includes(toimintaalue),
        R.view(lens)
      ),
      (_, model) =>
        R.set(
          lens,
          R.compose(
            R.uniq,
            R.ifElse(
              R.includes(toimintaalue),
              R.reject(R.equals(toimintaalue)),
              R.append(toimintaalue)
            ),
            R.view(lens)
          )(model),
          model
        )
    );
</script>

<style type="text/postcss">
  ol {
    @apply flex flex-row flex-wrap;
  }

  li {
    @apply flex-grow w-1/3 mb-6;
  }

  span {
    @apply text-secondary mb-4;
  }
</style>

<span>{label}</span>
<ol>
  {#each R.sortWith([R.ascend(format)], toimintaalueet) as toimintaalue}
    <li>
      <Checkbox
        label={format(toimintaalue)}
        disabled={disabled || ToimintaAlueUtils.isMainToimintaAlue(mainToimintaalue, toimintaalue) || (R.compose( R.lte(limit), R.length )(selected) && R.compose( R.not, R.includes(toimintaalue) )(selected))}
        lens={toimintaalueLens(toimintaalue)}
        bind:model />
    </li>
  {/each}
</ol>
