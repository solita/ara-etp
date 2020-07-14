<script>
  import * as R from 'ramda';

  export let label = '';
  export let group = 0;
  export let value = 0;
  export let disabled = false;

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

  label.disabled {
    @apply text-disabled;
  }

  label:hover ~ span {
    @apply bg-background;
  }

  label:hover ~ span.selected {
    @apply bg-light;
  }

  span {
    @apply relative w-6 h-6 rounded-full border-2 border-dark mr-2 bg-light cursor-pointer;
  }

  span.disabled {
    @apply border-disabled bg-lighterdisabled;
  }

  span:hover {
    @apply bg-background;
  }

  span.focused {
    @apply bg-secondary;
  }

  span.selected {
    @apply border-primary;
  }

  span.selected.disabled {
    @apply border-disabled;
  }

  span.selected:hover {
    @apply bg-light;
  }

  span.selected.disabled:hover {
    @apply bg-lighterdisabled;
  }

  span.selected.focused {
    @apply border-secondary bg-light;
  }

  span::after {
    @apply absolute hidden rounded-full;
    content: '';
    top: 15%;
    left: 15%;
    right: 15%;
    bottom: 15%;
  }

  span.selected::after {
    @apply block bg-primary;
  }

  span.selected.disabled::after {
    @apply block bg-disabled;
  }

  span.selected.focused::after {
    @apply bg-secondary;
  }
</style>

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label class:disabled>
    {label}
    <input {disabled} type="radio" bind:group {value} />
  </label>
  <span
    class:disabled
    class:focused
    class:selected
    on:click={_ => disabled || (group = value)}
    on:click />
</div>
