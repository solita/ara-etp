<script>
  import * as R from 'ramda';

  export let label = '';
  export let disabled = false;

  export let model;
  export let lens = R.identity;

  export let format = R.identity;
  export let parse = R.identity;

  let focused = false;

  $: checked = format(R.view(lens, model));
</script>

<style type="text/postcss">
  div {
    @apply flex flex-row-reverse relative justify-end items-center;
  }

  input {
    @apply opacity-0 w-0 h-0;
  }

  label {
    @apply cursor-pointer;
  }

  label.disabled {
    @apply text-disabled cursor-default;
  }

  label .material-icons {
    @apply select-none text-3xl;
  }

  .disabled .material-icons {
    @apply text-disabled;
  }
</style>

<!-- purgecss: inline-block hidden disabled font-bold -->

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label class:disabled class:font-bold={focused}>
    <span
      class="material-icons checked text-primary align-bottom"
      class:inline-block={checked}
      class:hidden={!checked}>check_box</span>
    <span
      class="material-icons unchecked align-bottom"
      class:inline-block={!checked}
      class:hidden={checked}>check_box_outline_blank</span>
    <span class="align-middle">{label}</span>
    <input
      {disabled}
      type="checkbox"
      {checked}
      on:change={() => !disabled && (model = R.set(lens, parse(!checked), model))} />
  </label>
</div>
