<script>
  import * as R from 'ramda';

  import InputContainer from './InputContainer';
  import SimpleInput from './SimpleInput';

  export let id;
  export let name;

  export let label = '';
  export let caret = false;
  export let search = false;
  export let required = false;
  export let autocomplete = 'off';
  export let disabled = false;
  export let compact = false;
  export let center = false;

  export let model = { empty: '' };
  export let lens = R.lensProp('empty');
  export let currentValue = '';

  export let parse = R.identity;
  export let format = R.identity;
  export let valid = true;
  export let validators = [];

  export let i18n;
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

  .inputwrapper.caret::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    content: 'expand_more';
  }

  .inputwrapper.search::after {
    @apply font-icon absolute text-2xl font-bold text-disabled;
    right: 0.5em;
    content: 'search';
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

<InputContainer
  {lens}
  bind:model
  bind:currentValue
  {i18n}
  {parse}
  {format}
  {validators}
  on:keydown
  bind:valid
  let:viewValue
  let:errorMessage>
  <SimpleInput
    {id}
    {name}
    {label}
    {caret}
    {search}
    {required}
    {autocomplete}
    {disabled}
    {compact}
    {center}
    {viewValue}
    {valid} />

  {#if !valid}
    <div class="error-label">
      <span class="font-icon error-icon">error</span>
      {errorMessage}
    </div>
  {/if}
</InputContainer>
