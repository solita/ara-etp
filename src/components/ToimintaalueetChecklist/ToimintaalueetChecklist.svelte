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

  export let mainToimintaalue;
  export let model;
  export let lens;

  export let format = R.identity;
  export let parse = R.identity;

  const selectedToModel = R.compose(
    R.map(
      R.compose(
        parseInt,
        R.head
      )
    ),
    R.filter(R.last),
    R.toPairs
  );

  $: toimintaalueetWithoutMain = R.compose(
    Maybe.orSome(toimintaalueet),
    R.map(
      R.compose(
        R.applyTo(toimintaalueet),
        R.reject,
        R.equals
      )
    )
  )(mainToimintaalue);

  $: selected = R.reduce(
    (acc, i) => R.assoc(i, R.includes(i, R.view(lens, model)), acc),
    {},
    toimintaalueetWithoutMain
  );

  $: model = R.over(
    lens,
    ToimintaAlueUtils.toimintaalueetWithoutMain(mainToimintaalue),
    model
  );

  $: toimintaalueLens = toimintaalue =>
    R.lens(
      _ =>
        R.prop(toimintaalue, selected) ||
        R.compose(
          Maybe.isSome,
          R.filter(R.equals(toimintaalue))
        )(mainToimintaalue),
      (value, _) =>
        R.set(
          lens,
          R.compose(
            selectedToModel,
            R.assoc(toimintaalue, value)
          )(selected),
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
        disabled={!R.prop(toimintaalue, selected) && (R.compose( Maybe.isSome, R.filter(R.equals(toimintaalue)) )(mainToimintaalue) || ToimintaAlueUtils.isLimit(limit, selected))}
        lens={toimintaalueLens(toimintaalue)}
        bind:model />
    </li>
  {/each}
</ol>
