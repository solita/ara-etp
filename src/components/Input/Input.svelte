<script>
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as v from '@Utility/validation';
  import Label from '@Component/Label/Label';

  export let id;
  export let name;

  export let label = '';
  export let caret = false;
  export let required = false;
  export let autocomplete = 'off';
  export let disabled = false;
  export let compact = false;
  export let center = false;

  export let model = { empty: '' };
  export let lens = R.lensProp('empty');

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];

  $: viewValue = R.compose(
    Maybe.orSome(viewValue),
    Either.toMaybe,
    R.map(format),
    Either.fromValueOrEither,
    R.view(lens)
  )(model);

  let valid = true;
  let errorMessage = '';
  let focused = false;

  export let i18n;

  $: highlightError = focused && !valid;

  $: validate = value =>
    v.validateModelValue(validators, value).cata(
      error => {
        valid = false;
        errorMessage = error(i18n);
      },
      () => (valid = true)
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
    @apply w-full relative font-medium py-1;
  }

  input.center {
    @apply text-center;
  }

  input:focus {
    @apply outline-none;
  }

  input:hover {
    @apply bg-background;
  }

  input:disabled {
    @apply bg-light;
  }

  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }

  .error-icon {
    @apply text-error;
  }
</style>

<Label {id} {required} {label} {compact} error={highlightError} {focused} />
<div
  class="inputwrapper"
  class:caret
  class:focused
  class:error={highlightError}
  class:disabled>
  <input
    {id}
    {name}
    {disabled}
    class="input"
    class:center
    class:error={highlightError}
    type="text"
    {autocomplete}
    value={viewValue}
    on:focus={event => {
      focused = true;
      validate(parse(event.target.value));
    }}
    on:blur={event => {
      focused = false;
      const parsedValue = parse(event.target.value);
      Either.fromValueOrEither(parsedValue).forEach(() => (viewValue = ''));
      model = R.set(lens, parsedValue, model);
      validate(parsedValue);
    }}
    on:click
    on:keydown
    on:input={event => {
      validate(parse(event.target.value));
    }} />
</div>

{#if !valid}
  <div class="error-label">
    <span class="font-icon error-icon">error</span>
    {errorMessage}
  </div>
{/if}
