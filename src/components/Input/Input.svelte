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
  export let warnValidators = [];

  export let labelUnit;

  let validationResult = {
    type: '',
    message: ''
  };

  export let i18n;
</script>

<style type="text/postcss">
  .validation-label {
    @apply absolute top-auto;
    font-size: smaller;
  }
</style>

<InputContainer
  {lens}
  bind:model
  bind:currentValue
  {id}
  {i18n}
  {parse}
  {format}
  {validators}
  {warnValidators}
  tooltip={compact ? label : null}
  on:keydown
  bind:valid
  bind:validationResult
  let:viewValue>
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
    {valid}
    {validationResult}
    {labelUnit} />

  {#if !valid}
    <div class="validation-label">
      {#if validationResult.type === 'error'}
        <span class="font-icon text-error">error</span>
      {:else if validationResult.type === 'warning'}
        <span class="font-icon text-warning">warning</span>
      {/if}
      {validationResult.message}
    </div>
  {/if}
</InputContainer>
