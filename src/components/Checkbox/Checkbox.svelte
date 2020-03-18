<script>
  import * as R from 'ramda';

  export let label = '';
  export let model = false;
  export let lens = R.identity;

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
    @apply text-secondary cursor-pointer;
  }

  label:hover ~ span {
    @apply bg-disabled;
  }

  span {
    @apply relative w-6 h-6 rounded-sm mr-2 bg-background cursor-pointer;
  }

  span:hover {
    @apply bg-disabled;
  }

  span.focused {
    @apply bg-disabled;
  }

  span.checked {
    @apply bg-primary;
  }

  span.checked.focused {
    @apply bg-secondary;
  }

  span.checked:hover {
    @apply bg-background;
  }

  span.checked::after {
    content: '';
    top: 15%;
    left: 33%;
    right: 33%;
    bottom: 33%;
    @apply absolute border-b-3 border-r-3 border-light;

    transform: rotate(45deg);
  }

  span.checked:hover::after {
    @apply border-primary;
  }
</style>

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label>
    {label}
    <input
      type="checkbox"
      checked={R.view(lens, model)}
      on:change={event => (model = R.set(lens, event.target.checked, model))} />
  </label>
  <span
    class:focused
    class:checked
    on:click={_ => (model = R.set(lens, !checked, model))} />
</div>
