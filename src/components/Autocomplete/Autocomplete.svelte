<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '../../utils/maybe-utils';

  import {
    focusNode,
    isHandlableKey,
    handleKey
  } from './autocomplete-navigation';

  import Input from '../Input/Input.svelte';
  import DropdownList from '../DropdownList/DropdownList.svelte';

  export let state = {};

  let inputNode = [];
  let dropdownNodes = [];

  let focusedIndex = Maybe.None();

  $: showDropdown = focusedIndex.isSome();
  $: focusableNodes = [inputNode, ...dropdownNodes];

  const handleKeydown = event => {
    if (!isHandlableKey(event.keyCode)) {
      return true;
    }

    focusedIndex = R.compose(
      Maybe.fromNull,
      Maybe.fold(null, R.tap(_ => event.preventDefault())),
      Maybe.fromNull,
      R.chain(handleKey(focusableNodes, event))
    )(focusedIndex);
  };
</script>

<style type="text/postcss">
  div {
    @apply relative;
  }
</style>

<div on:keydown={handleKeydown}>
  <Input
    {...state.input}
    passFocusableNodesToParent={node => (inputNode = node)}
    on:focus={_ => (focusedIndex = Maybe.Some(0))}
    on:blur={_ => (focusedIndex = Maybe.None())} />
  {#if showDropdown && state.dropdown}
    <DropdownList
      state={state.dropdown}
      passFocusableNodesToParent={nodes => (dropdownNodes = nodes)}
      on:click={_ => (focusedIndex = Maybe.None())}
      on:blur={_ => (focusedIndex = Maybe.None())} />
  {/if}
</div>
