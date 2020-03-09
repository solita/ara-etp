<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let id;
  export let name;

  export let label = '';
  export let caret = false;
  export let required = false;
  export let passFocusableNodesToParent = () => {};
  export let autocomplete = 'off';
  export let disabled = false;

  export let model;
  export let lens;

  export let parse = R.identity;
  export let format = R.identity;
  export let validation = R.always(true);

  let value = format(R.view(lens, model));

  let valid = false;
  let focused = false;

  $: highlightError = focused && !valid

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

  label.error,
  label.error::before {
    @apply text-error;
  }

  label.focused,
  label.focused::before {
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

  .inputwrapper.disabled {
    @apply border-0 pb-3;
  }

  input {
    @apply w-full relative font-extrabold py-1;
  }

  input:focus {
    @apply outline-none;
  }

  input:hover {
    @apply bg-background;
  }

  input:disabled {
    @apply bg-background;
  }

  input[type='number']::-webkit-inner-spin-button,
  input[type='number']::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
</style>

<label for={id} class:required class:error={highlightError} class:focused>{label}</label>

<div class="inputwrapper" class:caret class:focused class:error={highlightError} class:disabled>
  <input
    {id}
    {name}
    {disabled}
    class="input"
    class:error={highlightError}
    type="text"
    {autocomplete}
    bind:value
    bind:this={inputNode}
    on:focus={_ => {
      focused = true;
    }}
    on:blur={event => {
      focused = false;
      model = R.set(lens, parse(value), model);
      valid = validation(parse(value));
    }}
    on:click
    on:keydown
    on:input={event => {
      valid = validation(parse(value));
    }} />
</div>
