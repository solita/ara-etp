<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as keys from '@Utility/keys';

  import DropdownList from '@Component/DropdownList/DropdownList';

  export let items = [];
  export let format = R.identity;
  export let parse = R.identity;
  export let label = '';

  export let disabled = false;
  export let required = false;

  export let model;
  export let lens;

  let focused = false;
  let node;
  let button;

  let active = Maybe.None();

  $: selected = R.compose(
    R.when(R.complement(Maybe.isMaybe), Maybe.of),
    R.view(lens)
  )(model);

  $: showDropdown = Maybe.isSome(active);

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
      if (showDropdown) {
        event.preventDefault();
        return R.compose(
          Maybe.None,
          R.forEach(item => {
            model = R.set(lens, parse(item), model);
          }),
          R.chain(selectedItem(items))
        )(active);
      }
      return Maybe.Some(0);
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

  span.required::before {
    @apply font-icon text-xs align-top;
    content: '* ';
  }

  div {
    @apply relative;
  }

  .button {
    @apply font-extrabold min-h-2.5em block py-2 cursor-pointer border-b-3 border-disabled;
  }

  .button:hover {
    @apply bg-background border-primary;
  }

  .button:focus {
    @apply outline-none border-primary;
  }

  .button.disabled {
    @apply bg-background border-0 min-h-0 py-1 cursor-default;
  }

  .focused {
    @apply font-extrabold;
  }

  .focused.disabled {
    @apply font-normal;
  }

  .label {
    @apply text-secondary;
  }
</style>

<svelte:window
  on:click={event => {
    const itemNodes = node.querySelectorAll('.dropdownitem');
    if (!R.includes(event.target, itemNodes) && event.target !== button) {
      active = Maybe.None();
    }
  }} />

<span class:focused class:required class:disabled class="label">{label}</span>
<div bind:this={node} on:keydown={handleKeydown}>
  <span
    class:disabled
    bind:this={button}
    class="button"
    tabindex={disabled ? -1 : 0}
    on:click={_ => disabled || (showDropdown = !showDropdown)}
    on:focus={_ => {
      focused = true;
    }}
    on:blur={_ => {
      focused = false;
    }}>
    {R.compose( Maybe.orSome($_('validation.no-selection')), R.map(format) )(selected)}
  </span>
  {#if showDropdown}
    <DropdownList
      items={R.map(format, items)}
      {active}
      onclick={(item, index) => {
        model = R.set(lens, parse(R.nth(index, items)), model);
        active = Maybe.None();
      }} />
  {/if}
</div>
