<script>
  import { tick } from 'svelte';

  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as keys from '@Utility/keys';

  import DropdownList from '@Component/DropdownList/DropdownList';

  export let id;
  export let items = [];
  export let format = R.identity;
  export let parse = R.identity;
  export let inputValueParse;
  export let label = '';

  export let disabled = false;
  export let required = false;
  export let allowNone = true;
  export let noneLabel = 'validation.no-selection';

  export let name = '';

  export let model;
  export let lens;

  let input;
  let focused = false;
  let node;
  let button;

  let active = Maybe.None();

  $: selected = R.compose(
    R.when(R.complement(Maybe.isMaybe), Maybe.of),
    R.view(lens)
  )(model);

  $: showDropdown = Maybe.isSome(active);

  $: selectableItems = allowNone
    ? [$_(noneLabel), ...R.map(format, items)]
    : R.map(format, items);

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
    @apply font-medium cursor-pointer border-b-3 pt-2 border-disabled;
  }

  .button:not(.disabled)::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    top: 0.03em;
    content: 'expand_more';
  }

  .button.focused:not(.disabled)::after {
    @apply text-primary;
  }

  .button:hover {
    @apply bg-background border-primary;
  }

  .button:focus {
    @apply outline-none border-primary;
  }

  .button.disabled {
    @apply bg-light border-0 min-h-0 py-1 cursor-default;
  }

  .label.focused {
    @apply font-extrabold;
  }

  .label.focused.disabled {
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

<!-- purgecss: disabled -->
<span class:focused class:required class:disabled class="label">{label}</span>
<div bind:this={node} on:keydown={handleKeydown}>
  <input
    bind:this={input}
    class="sr-only"
    tabindex="-1"
    {name}
    value={(inputValueParse || parse)(R.view(lens, model))} />
  <div
    {id}
    class:disabled
    bind:this={button}
    class="button flex items-center"
    class:focused
    tabindex={disabled ? -1 : 0}
    on:click={_ => disabled || (showDropdown = !showDropdown)}
    on:focus={_ => {
      focused = true;
    }}
    on:blur={_ => {
      focused = false;
    }}>
    {R.compose( Maybe.orSome($_(noneLabel)), R.map(format) )(selected)}
  </div>
  {#if showDropdown}
    <DropdownList
      items={selectableItems}
      {active}
      onclick={async (item, index) => {
        if (allowNone && index === 0) {
          model = R.set(lens, Maybe.None(), model);
        } else {
          model = R.compose( R.set(lens, R.__, model), parse, R.nth(R.__, items), R.when(R.always(allowNone), R.dec) )(index);
        }
        active = Maybe.None();
        await tick();
        const event = new Event('change', { bubbles: true });
        input.dispatchEvent(event);
      }} />
  {/if}
</div>
