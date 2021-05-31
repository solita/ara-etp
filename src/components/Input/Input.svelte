<script>
  import * as R from 'ramda';

  import Label from '@Component/Label/Label';
  import InputContainer from './InputContainer';
  import InputField from './InputField';
  import SquareInputWrapper from './SquareInputWrapper';

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

  export let model = '';
  export let lens = R.identity;
  export let currentValue = '';

  export let parse = R.identity;
  export let format = R.identity;

  export let validators = [];
  export let warnValidators = [];

  export let labelUnit;

  export let inputComponentWrapper = SquareInputWrapper;
  export let inputComponent = InputField;

  export let i18n;

  let focused = false;
</script>

<style type="text/postcss">
  .validation-label {
    @apply absolute top-auto font-normal bg-light z-10;
    font-size: smaller;
  }
</style>

<InputContainer
  {lens}
  bind:model
  bind:currentValue
  {id}
  {i18n}
  {disabled}
  {parse}
  {format}
  {validators}
  {warnValidators}
  tooltip={compact ? label : null}
  on:keydown
  let:valid
  let:validationResult
  let:viewValue
  let:focused
  let:warning
  let:error>
  <Label
    {label}
    {id}
    {required}
    {compact}
    {error}
    {warning}
    {focused}
    unit={labelUnit} />

  <slot {valid} {validationResult} {viewValue} {focused} {warning} {error} />

  {#if !$$slots.default}
    <svelte:component
      this={inputComponentWrapper}
      {caret}
      {search}
      {focused}
      {error}
      {warning}
      {disabled}>
      <svelte:component
        this={inputComponent}
        {id}
        {name}
        {disabled}
        {center}
        {error}
        {autocomplete}
        {viewValue}
        {focused}
        on:input
        on:keypress />
    </svelte:component>
  {/if}

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
