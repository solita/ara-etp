<script>
  import * as R from 'ramda';

  export let label = '';
  export let group = 0;
  export let value = 0;

  let focused = false;

  $: selected = R.equals(group, value);
</script>

<style type="text/postcss">
  div {
    @apply flex flex-row-reverse relative justify-end items-center;
  }

  input {
    @apply opacity-0 w-0 h-0;
  }

  label {
    @apply text-secondary cursor-pointer;
  }

  label:hover ~ span {
    @apply bg-disabled;
  }

  span {
    @apply relative w-6 h-6 rounded-full mr-2 bg-background cursor-pointer;
  }

  span:hover {
    @apply bg-disabled;
  }

  span.focused {
    @apply bg-disabled;
  }

  span.selected {
    @apply bg-primary;
  }

  span.selected.focused {
    @apply bg-secondary;
  }

  span::after {
    @apply absolute hidden rounded-full;
    content: '';
    top: 33%;
    left: 33%;
    right: 33%;
    bottom: 33%;

    transform: rotate(45deg);
  }

  span:hover::after {
    @apply block bg-light;
  }

  span.selected::after {
    @apply block bg-light;
  }
</style>

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label>
    {label}
    <input type="radio" bind:group {value} />
  </label>
  <span class:focused class:selected on:click={_ => (group = value)} />
</div>
