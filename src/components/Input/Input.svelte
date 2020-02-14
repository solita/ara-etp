<script>
  import { onMount } from 'svelte';

  export let value = '';
  export let label = '';
  export let error = false;
  export let caret = false;
  export let id = '';
  export let required = false;
  export let passFocusableNodesToParent = () => {};

  let focused = false;

  let inputNode;
  onMount(() => passFocusableNodesToParent(inputNode));
</script>

<style type="text/postcss">
  label {
    @apply text-secondary capitalize;
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

  input.error {
    @apply border-error;
  }

  input.error::after {
    @apply text-error;
  }
</style>

<label for={id} class:required class:error class:focused>{label}</label>

<div class="inputwrapper" class:caret class:focused class:error>
  <input
    {id}
    {required}
    bind:this={inputNode}
    on:focus={_ => (focused = true)}
    on:focus
    on:blur={_ => (focused = false)}
    on:blur
    on:click
    on:keydown
    class="input"
    class:error
    type="text"
    {value} />
</div>
