<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as v from '@Utility/validation';
  import Label from '@Component/Label/Label';
  import { autoresize } from './autoresize';

  export let id;
  export let required;
  export let label = '';
  export let disabled = false;
  export let autocomplete = 'off';
  export let name = '';

  export let model = { empty: '' };
  export let lens = R.lensProp('empty');

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];

  export let min = 10;
  export let max = 30;

  export let compact;

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
  }

  .inputwrapper:hover {
    @apply border-hover bg-background;
  }

  .inputwrapper.disabled:hover {
    @apply bg-light;
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
    @apply flex-grow font-medium p-2 resize-none bg-transparent;
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

<!-- purgecss: focused error disabled -->
{#if label}
  <Label {id} {required} {label} {compact} error={highlightError} {focused} />
{/if}
<div
  class="inputwrapper"
  class:focused
  class:error={highlightError}
  class:disabled>
  <textarea
    {id}
    {name}
    {disabled}
    {autocomplete}
    use:autoresize={[min, max]}
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
