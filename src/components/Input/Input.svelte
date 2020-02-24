<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';

  export let id;
  export let name;

  export let label = '';
  export let error = false;
  export let caret = false;
  export let required = false;
  export let passFocusableNodesToParent = () => {};
  export let type = 'text';
  export let pattern = null;
  export let autocomplete = 'off';
  export let value = '';
  export let valid = true;

  export let validation = R.always(true);

  export let update = () => {};

  let focused = false;

  let inputNode;
  onMount(() => passFocusableNodesToParent(inputNode));
</script>

<style type="text/postcss">
  label {
    @apply text-secondary;
  }

  label.required::before {
    @apply font-icon text-xs align-top;
    content: '*';
  }

  label.error {
    @apply text-error;
  }

  label.focused {
    @apply font-bold;
  }

  .inputwrapper {
    @apply flex relative items-center border-b-3 border-disabled text-dark;
  }

  .inputwrapper:hover {
    @apply border-hover;
  }

  .inputwrapper.focused {
    @apply border-primary;
  }

  .inputwrapper.focused::after {
    @apply text-primary;
  }

  .inputwrapper.error {
    @apply border-error;
  }

  .inputwrapper.error::after {
    @apply text-error;
  }

  .inputwrapper.caret::after {
    @apply font-icon absolute text-base text-disabled;
    right: 0.5em;
    content: 'expand_more';
  }
  input {
    @apply w-full relative font-extrabold py-1;
  }

  .input:focus {
    @apply outline-none;
  }

  .input:hover {
    @apply bg-background;
  }

  input[type='number']::-webkit-inner-spin-button,
  input[type='number']::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
</style>

<label for={id} class:required class:error class:focused>{label}</label>

<div class="inputwrapper" class:caret class:focused class:error>
  <input
    {id}
    {name}
    {required}
    class="input"
    class:error
    {type}
    {value}
    {autocomplete}
    {pattern}
    bind:this={inputNode}
    on:focus={_ => (focused = true)}
    on:focus
    on:blur={_ => (focused = false)}
    on:blur
    on:click
    on:keydown
    on:input={event => update(event.target.value)} />
</div>
