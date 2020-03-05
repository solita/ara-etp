<script>
  import { onMount } from 'svelte';

  import * as R from 'ramda';
  import * as keys from './keys';
  import * as Maybe from '../../utils/maybe-utils';

  import DropdownList from '../DropdownList/DropdownList';

  export let items = [];

  let active = Maybe.None();
  let input;
  let node;
  let filteredItems = [];

  function setValue(value) {
    input.value=value;
    input.dispatchEvent(new Event("input"));
  }

  const keyHandlers = {
    [keys.DOWN_ARROW]: active => active
          .map(R.compose(R.min(filteredItems.length - 1), R.add(1)))
          .orElse(Maybe.Some(0)),
    [keys.UP_ARROW]: active => active
          .map(R.add(-1))
          .filter(R.lte(0)),
    [keys.ESCAPE]: _ => Maybe.None(),
    [keys.TAB]: _ => Maybe.None(),
    [keys.ENTER]: active => {
      active.map(R.nth(R.__, filteredItems)).forEach(item => {
        setValue(item);
      });
      return Maybe.None();
    }
  };

  const handleKeydown = event => {
    const handler = keyHandlers[event.keyCode];
    if (!R.isNil(handler)) {
      active = handler(active);
    }
  };

  $: showDropdown = items.length > 0 && active.isSome();

  onMount(_ => {
    input = node.getElementsByTagName('input')[0];
    input.addEventListener('input', event => {
      const value = input.value;
      filteredItems = R.filter(R.includes(value), items);
      active = active.orElse(Maybe.Some(0))
    });
  });
</script>

<style type="text/postcss">
  div {
    @apply relative;
  }
</style>

<svelte:window on:click={event => {
    const itemNodes = node.querySelectorAll('.dropdownitem');

    if (!R.includes(event.target, itemNodes) &&
        input !== event.target) {
      active = Maybe.None();
    }
}}/>

<div bind:this={node}
     on:keydown={handleKeydown}>

  <slot></slot>
  {#if showDropdown}
    <DropdownList
      items={filteredItems}
      active={active.getOrElse(0)}
      onclick={(item, index) => {
        setValue(item);
        input.focus();
        active = Maybe.None();
      }}/>
  {/if}
</div>
