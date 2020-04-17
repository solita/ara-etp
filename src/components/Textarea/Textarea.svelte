<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as v from '@Utility/validation';
  import Label from '@Component/Label/Label';

  export let id;
  export let required;
  export let label = '';
  export let disabled = false;
  export let autocomplete = 'off';

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
  .inputwrapper {
    @apply flex items-stretch border-b-3 border-disabled text-dark;
    min-height: 20em;
  }

  .inputwrapper:hover {
    @apply border-hover;
  }

  .inputwrapper.focused {
    @apply border-primary;
  }
  .inputwrapper.error {
    @apply border-error;
  }

  .inputwrapper.disabled {
    @apply border-0 pb-3;
  }

  textarea {
    @apply flex-grow font-medium resize-none text-xl py-1;
  }

  textarea:focus {
    @apply outline-none;
  }

  textarea::-webkit-scrollbar {
    @apply w-2;
  }

  textarea::-webkit-scrollbar-track {
    @apply bg-background;
  }

  textarea::-webkit-scrollbar-thumb {
    @apply bg-disabled;
  }

  textarea::-webkit-scrollbar-thumb:hover {
    @apply bg-dark;
  }

  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }

  .error-icon {
    @apply text-error;
  }
</style>

<Label {id} {required} {label} error={highlightError} {focused} />
<div
  class="inputwrapper"
  class:focused
  class:error={highlightError}
  class:disabled>
  <textarea
    {id}
    {required}
    {disabled}
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
    on:input={event => validate(parse(event.target.value))} />
</div>

{#if !valid}
  <div class="error-label">
    <span class="font-icon error-icon">error</span>
    {errorMessage}
  </div>
{/if}
