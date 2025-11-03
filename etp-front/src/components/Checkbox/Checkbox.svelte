<script>
  import * as R from 'ramda';

  export let label = '';
  export let disabled = false;

  export let model;
  export let lens = R.identity;

  export let format = R.identity;
  export let parse = R.identity;
  export let dataCy = undefined;

  let focused = false;

  $: checked = format(R.view(lens, model));
</script>

<style type="text/postcss">
  div {
    @apply relative flex flex-row-reverse items-center justify-end;
  }

  input {
    @apply h-0 w-0 opacity-0;
  }

  label {
    @apply cursor-pointer;
  }

  label.disabled {
    @apply cursor-default text-disabled;
  }

  label .material-icons {
    @apply select-none text-3xl;
  }

  .disabled .material-icons {
    @apply text-disabled;
  }
</style>

<div on:focusin={_ => (focused = true)} on:focusout={_ => (focused = false)}>
  <label class:disabled class:font-bold={focused}>
    <span
      class="material-icons checked align-bottom text-primary"
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
      data-cy={dataCy}
      {checked}
      on:change={() =>
        !disabled && (model = R.set(lens, parse(!checked), model))} />
  </label>
</div>
