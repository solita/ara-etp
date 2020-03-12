<script>
  import { onMount } from 'svelte';
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as v from '@Utility/validation';

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
  export let validation = [{predicate: R.always(true), label: R.always('')}];

  $: value = Either.fromValueOrEither(R.view(lens, model))
      .map(format).toMaybe().orSome(value);

  let valid = true;
  let errorMessage = '';
  let focused = false;

  export let i18n;

  $: highlightError = focused && !valid;

  let inputNode;
  onMount(() => passFocusableNodesToParent(inputNode));

  const validate = (value) =>
    Either.fromValueOrEither(value)
      .flatMap(modelValue => v.validate(validation, modelValue).leftMap(R.prop('label')))
      .cata(
          error => {
            valid = false;
            errorMessage = error(i18n);
          },
          () => valid = true
      );

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

  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }

  .error-icon {
    @apply text-error;
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
    {value}
    bind:this={inputNode}
    on:focus={_ => {
      focused = true;
      validate(parse(inputNode.value));
    }}
    on:blur={event => {
      focused = false;
      const parsedValue = parse(inputNode.value);
      model = R.set(lens, parsedValue, model);
      validate(parsedValue);
    }}
    on:click
    on:keydown
    on:input={event => {
      validate(parse(inputNode.value));
    }} />
</div>

{#if !valid}
<div class="error-label">
  <span class="font-icon error-icon">error</span>
  {errorMessage}
</div>
{/if}