<script>
  import { slide } from 'svelte/transition';

  import Input from '../Input/Input.svelte';
  import DropdownList from '../DropdownList/DropdownList.svelte';

  let showDropdown = false;

  export let state = {};
</script>

<style>
  .autocomplete {
    @apply relative;
  }
  .dropdownwrapper {
    @apply absolute top-auto left-0 right-0;
  }
</style>

<div class="autocomplete">
  <Input
    {...state.input}
    on:focus={_ => (showDropdown = true)}
    on:blur={_ => (showDropdown = false)}
    on:click={_ => (showDropdown = !showDropdown)} />
  {#if showDropdown && state.dropdown}
    <div class="dropdownwrapper">
      <DropdownList state={state.dropdown} />
    </div>
  {/if}
</div>
