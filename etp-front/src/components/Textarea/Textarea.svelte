<script>
  import * as R from 'ramda';
  import { autoresize } from './autoresize';

  import * as Keys from '@Utility/keys';
  import Input from '@Component/Input/Input';
  import SquareInputWrapper from '@Component/Input/SquareInputWrapper';

  export let id;
  export let required;
  export let label = '';
  export let disabled = false;
  export let autocomplete = 'off';
  export let name = '';

  export let model = '';
  export let lens = R.identity;

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];
  export let warnValidators = [];

  export let min = 10;
  export let max = 30;

  export let compact;
  export let i18n;
</script>

<style>
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
</style>

<Input
  {id}
  {name}
  {required}
  {disabled}
  {label}
  {parse}
  {format}
  {validators}
  {warnValidators}
  {compact}
  {i18n}
  {lens}
  bind:model
  let:api
  let:viewValue
  let:focused
  let:warning
  let:error>
  <SquareInputWrapper {focused} {error} {warning} {disabled}>
    <textarea
      class="w-full outline-none"
      {id}
      {name}
      data-cy={name}
      {disabled}
      {autocomplete}
      aria-describedby={`${id}-error-label`}
      aria-invalid={error && viewValue.length > 0 ? 'true' : 'false'}
      use:autoresize={[min, max]}
      value={viewValue}
      on:click
      on:focus={api.focus}
      on:blur={event => api.blur(event.target.value)}
      on:input={event => api.input(event.target.value)}
      on:keydown={event => {
        if (event.keyCode === Keys.ENTER) {
          api.update(event.target.value);
        }
      }} />
  </SquareInputWrapper>
</Input>
