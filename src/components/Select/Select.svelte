<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as keys from '@Utility/keys';

  import DropdownList from '@Component/DropdownList/DropdownList';

  export let items = [];
  export let format = R.identity;
  export let parse = R.identity;

  export let model;
  export let lens;

  $: selected = Maybe.fromNull(R.view(lens, model));

  let showDropdown = false;
  let active = Maybe.None();

  const previousItem = R.compose(
    R.filter(R.lte(0)),
    Maybe.Some,
    R.dec
  );

  const nextItem = R.curry((items, active) =>
    R.compose(
      Maybe.Some,
      R.min(R.inc(active)),
      R.dec,
      R.length
    )(items)
  );

  const selectedItem = R.curry((items, active) =>
    R.compose(
      Maybe.fromNull,
      R.nth(R.__, items)
    )(active)
  );

  const keyHandlers = {
    [keys.DOWN_ARROW]: (_, active) => {
      if (showDropdown) event.preventDefault();
      return R.compose(
        Maybe.orElse(Maybe.Some(0)),
        R.chain(nextItem(items))
      )(active);
    },
    [keys.UP_ARROW]: (event, active) => {
      if (showDropdown) event.preventDefault();
      return R.chain(previousItem, active);
    },
    [keys.ESCAPE]: (_, active) => Maybe.None(),
    [keys.TAB]: (_, active) => Maybe.None(),
    [keys.ENTER]: (event, active) => {
      if (showDropdown) event.preventDefault();
      const newActive = R.compose(
        R.forEach(item => {
          selected = item;
          showDropdown = false;
        }),
        R.chain(selectedItem(items))
      )(active);
    }
  };

  const handleKeydown = event => {
    R.compose(
      R.forEach(handler => (active = handler(event, active))),
      Maybe.fromNull,
      R.prop(R.__, keyHandlers),
      R.prop('keyCode')
    )(event);
  };
</script>

<style type="text/postcss">
  div {
    @apply relative;
  }
  span {
    @apply min-h-2.5em block py-2 border-b-3 border-disabled cursor-pointer;
  }
</style>

<div on:keydown={handleKeydown}>
  <span tabindex="0" on:click={_ => (showDropdown = !showDropdown)}>
    {R.compose( Maybe.orSome($_('validation.no-selection')), R.map(format) )(selected)}
  </span>
  {#if showDropdown}
    <DropdownList
      items={R.map(format, items)}
      {active}
      onclick={(item, index) => {
        model = R.set(lens, parse(item), model);
        showDropdown = false;
      }} />
  {/if}
</div>
