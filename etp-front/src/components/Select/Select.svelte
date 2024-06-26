<script>
  import { tick } from 'svelte';

  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as keys from '@Utility/keys';
  import * as Validation from '@Utility/validation';

  import DropdownList from '@Component/DropdownList/DropdownList';

  export let id;
  export let items = [];
  export let format = R.identity;
  export let parse = R.identity;
  export let inputValueParse;
  export let label = '';

  export let compact = false;
  export let disabled = false;
  export let required = false;
  export let allowNone = true;
  export let noneLabel = 'validation.no-selection';
  export let validation = false;

  export let name = '';

  export let model;
  export let lens = R.identity;
  export let validators = [];

  export let itemComponent = null;

  const i18n = $_;

  let input;
  let focused = false;
  let node;
  let button;
  let blurred = false;

  let active = Maybe.None();

  $: selected = R.compose(
    R.when(R.complement(Maybe.isMaybe), Maybe.of),
    R.view(lens)
  )(model);

  $: showDropdown = Maybe.isSome(active);

  $: selectableItems = allowNone
    ? [$_(noneLabel), ...R.map(format, items)]
    : R.map(format, items);

  const previousItem = R.compose(R.filter(R.lte(0)), Maybe.Some, R.dec);

  const nextItem = R.curry((items, active) =>
    R.compose(
      Maybe.Some,
      R.min(R.inc(active)),
      R.when(() => !allowNone, R.dec),
      R.length
    )(items)
  );

  const selectedItem = R.curry((items, index) => {
    if (allowNone && index === 0) {
      return Maybe.None();
    } else {
      return parse(items[allowNone ? index - 1 : index]);
    }
  });

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
          item => {
            model = R.set(lens, item, model);
          },
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

  $: validationError = disabled
    ? Maybe.None()
    : R.compose(
        Either.toMaybe,
        Either.swap,
        Validation.validateModelValue(validators),
        R.view(lens)
      )(model);
</script>

<style type="text/postcss">
  .required-marker {
    @apply font-icon text-xs align-top mr-1;
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
    @apply bg-light border-transparent min-h-0 cursor-default;
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

  .validation-label {
    @apply absolute top-auto z-10;
    font-size: smaller;
  }
</style>

<svelte:window
  on:click={event => {
    const itemNodes = node.querySelectorAll('.dropdownitem');
    if (!R.includes(event.target, itemNodes) && event.target !== button) {
      active = Maybe.None();
    }
  }} />

<div>
  {#if required}
    <span class="required-marker" aria-hidden="true">*</span>
  {/if}
  <span
    class:focused
    class:required
    class:disabled
    class:sr-only={compact}
    class="label">{label}</span>
  {#if required}
    <span class="sr-only">({i18n('validation.required')})</span>
  {/if}
</div>
<div
  class="input-container"
  bind:this={node}
  on:keydown={handleKeydown}
  on:validation={_ => {
    blurred = true;
  }}>
  <input
    bind:this={input}
    class="hidden"
    tabindex="-1"
    {name}
    value={(inputValueParse || parse)(R.view(lens, model))}
    on:change />
  <div
    {id}
    data-cy={name}
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
    {R.compose(Maybe.orSome($_(noneLabel)), R.map(format))(selected)}
  </div>
  {#if showDropdown}
    <DropdownList
      items={selectableItems}
      {active}
      component={itemComponent}
      onclick={async (item, index) => {
        model = R.set(lens, selectedItem(items, index), model);
        active = Maybe.None();
        await tick();
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }} />
  {/if}

  {#if validation && required && Maybe.isNone(selected) && blurred}
    <div class="validation-label">
      <span class="font-icon text-error">error</span>
      {i18n('validation.required')}
    </div>
  {/if}
  {#each Maybe.toArray(validationError) as msg}
    <div class="validation-label">
      <span class="font-icon text-error">error</span>
      {msg(i18n)}
    </div>
  {/each}
</div>
