<script>
  import { onMount } from 'svelte';

  import * as R from 'ramda';
  import * as keys from '@Utility/keys';
  import * as Maybe from '@Utility/maybe-utils';

  import * as AutocompleteUtils from './autocomplete-utils';

  import DropdownList from '@Component/DropdownList/DropdownList';

  export let items = [];

  let active = Maybe.None();
  let input;
  let node;
  let filteredItems = [];

  const setInputValue = value => {
    input.value = value;
    input.dispatchEvent(new Event('input'));
  };

  $: showDropdown = items.length > 0 && active.isSome();

  const keyHandlers = {
    [keys.DOWN_ARROW]: (_, active) => {
      if (showDropdown) event.preventDefault();
      return R.compose(
        Maybe.orElse(Maybe.Some(0)),
        R.chain(AutocompleteUtils.nextItem(filteredItems))
      )(active);
    },
    [keys.UP_ARROW]: (event, active) => {
      if (showDropdown) event.preventDefault();
      return R.chain(AutocompleteUtils.previousItem, active);
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
    const value = input.value;
    filteredItems = R.compose(
        R.take(5),
        R.filter(
            R.compose(
                R.includes(R.toLower(value)),
                R.toLower
            )
        )
    )(items);
    active = active.orElse(Maybe.Some(0));
  }

  onMount(_ => {
    input = node.getElementsByTagName('input')[0];
    input.addEventListener('input', inputHandler);
    input.addEventListener('focus', inputHandler);
  });
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

  <slot />
  {#if showDropdown}
    <DropdownList
      items={filteredItems}
      {active}
      onclick={(item, index) => {
        setInputValue(item);
        input.focus();
        active = Maybe.None();
      }} />
  {/if}
</div>
