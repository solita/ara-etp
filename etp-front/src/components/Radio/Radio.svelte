<script>
  import * as R from 'ramda';

  export let label = '';
  export let group = 0;
  export let value = 0;
  export let disabled = false;
  export let name = '';

  let focused = false;

  $: selected = R.equals(group, value);
</script>

<style type="text/postcss">
  div {
    @apply relative flex flex-row-reverse items-center justify-end;
  }

  input {
    @apply h-0 w-0 opacity-0;
  }

  label {
    @apply cursor-pointer text-secondary;
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
    @apply relative mr-2 h-6 w-6 cursor-pointer rounded-full border-2 border-dark bg-light;
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
  <label data-cy={name} class:disabled>
    {label}
    <input {disabled} type="radio" {name} bind:group {value} />
  </label>
  <span
    class:disabled
    class:focused
    class:selected
    on:click={_ => disabled || (group = value)}
    on:click />
</div>
