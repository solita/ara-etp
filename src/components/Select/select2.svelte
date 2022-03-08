<script>
  import { tick } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as keys from '@Utility/keys';
  import * as Validation from '@Utility/validation';
  import * as Objects from '@Utility/objects';
  import * as Parsers from '@Utility/parsers';
  import * as Future from '@Utility/future-utils';

  import { _ } from '@Language/i18n';
  import DropdownList from '@Component/DropdownList/DropdownList';
  import Input from '@Component/Input/Input';

  export let id;
  export let label = '';
  export let name = '';

  export let compact = false;
  export let disabled = false;
  export let required = false;

  export let model;
  export let lens = R.identity;
  export let items = [];
  export let format = R.identity;
  export let itemToModel = R.identity;
  export let modelToItem = R.identity;
  export let searchable = false;

  export let validators = [];

  const i18n = $_;

  let input;
  let focused = false;
  let node;
  let button;
  let blurred = false;

  let active = Maybe.None();
  let searchText = Maybe.None();
  let textCancel = () => {};

  $: modelValue = R.view(lens, model);
  $: selected = Objects.requireNotNil(
    modelToItem(modelValue),
    'Select ' +
      name +
      '#' +
      id +
      ' - item is not found by model value: ' +
      modelValue
  );

  $: showDropdown = Maybe.isSome(active);

  let selectableItems = items;
  $: {
    selectableItems = items;
    R.forEach(txt => {
      selectableItems = R.filter(
        R.compose(R.includes(R.toLower(txt)), R.toLower, format),
        items
      );
    }, searchText);
  }

  const previousItem = R.compose(R.filter(R.lte(0)), Maybe.Some, R.dec);

  const nextItem = R.curry((items, active) =>
    R.compose(R.min(R.inc(active)), R.dec, R.length)(items)
  );

  const setModel = index => {
    model = R.set(lens, itemToModel(R.nth(index, selectableItems)), model);
  };

  const keyHandlers = {
    [keys.DOWN_ARROW]: (event, active) => {
      if (showDropdown) event.preventDefault();
      return R.compose(
        Maybe.orElse(Maybe.Some(0)),
        R.map(nextItem(selectableItems))
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
        R.forEach(setModel, active);
        return Maybe.None();
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
  span.required::before {
    @apply font-icon text-xs align-top mr-1;
    content: '*';
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
    const searchInput = node.querySelectorAll('.selectsearch .input');
    if (
      !R.includes(event.target, itemNodes) &&
      !R.includes(event.target, searchInput) &&
      event.target !== button
    ) {
      active = Maybe.None();
    } else if (searchable) {
      R.head(searchInput).focus();
    }
  }} />

<!-- purgecss: disabled -->
<span
  class:focused
  class:required
  class:disabled
  class:sr-only={compact}
  class="label">{label}</span>
<div
  class="input-container"
  bind:this={node}
  on:keydown={handleKeydown}
  on:validation={_ => {
    blurred = true;
  }}>
  <input
    bind:this={input}
    class="sr-only"
    tabindex="-1"
    {name}
    value={selected}
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
    {format(selected)}
  </div>
  {#if showDropdown}
    <div class="absolute listcontainer w-full">
      {#if searchable}
        <div class="w-full p-2 bg-light shadow-dropdownlist selectsearch">
          <Input
            model={searchText}
            format={Maybe.orSome('')}
            parse={Parsers.optionalString}
            lens={R.identity}
            search={true}
            on:input={evt => {
              textCancel();
              textCancel = Future.value(value => {
                searchText = value;
              }, Future.after(500, Maybe.fromEmpty(R.trim(evt.target.value))));
            }} />
        </div>
      {/if}
      <DropdownList
        items={R.map(format, selectableItems)}
        {active}
        onclick={async (item, index) => {
          setModel(index);
          active = Maybe.None();
          await tick();
          input.dispatchEvent(new Event('change', { bubbles: true }));
        }} />
    </div>
  {/if}

  {#each Maybe.toArray(validationError) as msg}
    <div class="validation-label">
      <span class="font-icon text-error">error</span>
      {msg(i18n)}
    </div>
  {/each}
</div>
