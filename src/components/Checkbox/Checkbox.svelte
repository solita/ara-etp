<script>
  import * as R from 'ramda';

  export let label = '';
  export let disabled = false;

  export let model;
  export let lens;

  let focused = false;

  $: checked = R.view(lens, model);
</script>

<style type="text/postcss">
  div {
    @apply flex flex-row-reverse relative justify-end items-center;
  }

  input {
    @apply opacity-0 w-0 h-0;
  }

  label {
    @apply text-dark cursor-pointer;
  }

  label.disabled {
    @apply text-disabled cursor-default;
  }

  label:hover ~ span {
    @apply bg-disabled;
  }

  label:hover ~ span:not(.checked)::after {
    @apply text-primary;
  }

  span {
    @apply relative w-6 h-6 rounded-sm mr-2 bg-background cursor-pointer border-dark border-2;
  }

  span:hover {
    @apply bg-disabled;
  }

  span.focused {
    @apply bg-secondary;
  }

  span.checked {
    @apply bg-primary;
  }

  span.disabled {
    @apply bg-disabled cursor-default;
  }

  span.checked.focused {
    @apply bg-secondary;
  }

  span.checked:hover {
    @apply bg-disabled;
  }

  span.disabled:hover {
    @apply bg-disabled;
  }

  span.disabled.focused {
    @apply bg-disabled;
  }

  span.checked::after {
    content: 'done';

    @apply font-icon text-light absolute;
    top: -10%;
    left: 10%;
    right: 0%;
    bottom: 0%;
  }

  span.checked:hover::after {
    @apply text-primary;
  }

  span.checked.disabled:hover::after {
    @apply text-light;
  }
</style>

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label class:disabled>
    {label}
    <input
      {disabled}
      type="checkbox"
      {checked}
      on:change={() => !disabled && (model = R.set(lens, !checked, model))} />
  </label>
  <span
    class:disabled
    class:focused
    class:checked
    on:click={() => !disabled && (model = R.set(lens, !checked, model))} />
</div>
