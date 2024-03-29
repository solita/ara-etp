<script>
  import { onMount, onDestroy } from 'svelte';

  import * as R from 'ramda';
  import * as keys from '@Utility/keys';
  import * as Maybe from '@Utility/maybe-utils';

  import * as AutocompleteUtils from './autocomplete-utils';

  import DropdownList from '@Component/DropdownList/DropdownList';
  import Input from '@Component/Input/Input';
  import { announceAssertively } from '@Utility/aria-live';

  export let items = [];
  export let completedValue = '';
  export let size = 5;
  export let component = null;
  export let resultsAnnouncer = Maybe.None();

  let active = Maybe.None();
  let input;
  let node;
  let filteredItems = [];
  let rawValue = '';

  const setInputValue = value => {
    input.value = value;
    completedValue = value;
    input.blur();
    input.focus();
    input.dispatchEvent(new Event('change', { bubbles: true }));
    input.dispatchEvent(new Event('input', { bubbles: true }));
  };

  $: showDropdown = items.length > 0 && active.isSome();

  const keyHandlers = {
    [keys.DOWN_ARROW]: (event, active) => {
      if (showDropdown) event.preventDefault();
      return R.compose(
        R.tap(
          R.forEach(R.compose(announceAssertively, R.nth(R.__, filteredItems)))
        ),
        Maybe.orElse(Maybe.Some(0)),
        R.chain(AutocompleteUtils.nextItem(filteredItems))
      )(active);
    },
    [keys.UP_ARROW]: (event, active) => {
      if (showDropdown) event.preventDefault();
      return R.compose(
        R.tap(
          R.forEach(R.compose(announceAssertively, R.nth(R.__, filteredItems)))
        ),
        R.chain(AutocompleteUtils.previousItem)
      )(active);
    },
    [keys.ESCAPE]: (_, _active) => Maybe.None(),
    [keys.TAB]: (_, _active) => Maybe.None(),
    [keys.ENTER]: (event, active) => {
      if (showDropdown) event.preventDefault();

      return R.compose(
        R.always(Maybe.None()),
        R.forEach(setInputValue),
        R.chain(AutocompleteUtils.selectedItem(filteredItems))
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

  const inputHandler = () => {
    rawValue = input.value;
    active = active.orElse(Maybe.Some(0));
  };

  onMount(_ => {
    input = node.getElementsByTagName('input')[0];

    input.addEventListener('input', inputHandler);
    input.addEventListener('focus', inputHandler);
    input.addEventListener('click', inputHandler);
  });

  onDestroy(_ => {
    input.removeEventListener('input', inputHandler);
    input.removeEventListener('focus', inputHandler);
    input.removeEventListener('click', inputHandler);
  });

  $: filteredItems = R.compose(
    R.take(size),
    R.filter(R.compose(R.includes(R.toLower(rawValue)), R.toLower))
  )(items);

  $: if (showDropdown) {
    Maybe.fold(
      null,
      announcer => announcer(filteredItems.length),
      resultsAnnouncer
    );
  }
</script>

<style type="text/postcss">
  div {
    @apply relative;
  }
</style>

<svelte:window
  on:click={event => {
    const itemNodes = node.querySelectorAll('.dropdownitem');
    if (!R.includes(event.target, itemNodes) && input !== event.target) {
      active = Maybe.None();
    }
  }} />

<div bind:this={node} on:keydown={handleKeydown}>
  <slot>
    <Input bind:model={completedValue} />
  </slot>
  {#if showDropdown}
    <DropdownList
      items={filteredItems}
      {active}
      onclick={(item, index) => {
        setInputValue(item);
        active = Maybe.None();
      }}
      {component} />
  {/if}
</div>
