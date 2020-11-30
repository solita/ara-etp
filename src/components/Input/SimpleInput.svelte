<script>
  import Label from '@Component/Label/Label';
  import SquareInputWrapper from './SquareInputWrapper';
  import InputField from './InputField';

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
  export let type = 'text';
  export let placeholder = '';

  export let viewValue;
  export let rawValue = '';

  export let rawValueAsViewValue = false;

  export let valid = true;
  export let validationResult = {
    type: '',
    message: ''
  };

  export let wrapper = SquareInputWrapper;
  export let labelUnit;

  let error = false;
  let focused = false;

  $: rawValueAsViewValue && (viewValue = rawValue);

  $: error = focused && !valid && validationResult.type === 'error';
  $: warning = focused && !valid && validationResult.type === 'warning';
</script>

<Label
  {id}
  {required}
  {label}
  {compact}
  {error}
  {warning}
  {focused}
  unit={labelUnit} />
<svelte:component
  this={wrapper}
  {caret}
  {search}
  {focused}
  {error}
  {warning}
  {disabled}>
  <InputField
    {id}
    {name}
    {disabled}
    {center}
    {error}
    {autocomplete}
    {viewValue}
    {type}
    {placeholder}
    bind:rawValue
    bind:focused
    on:input
    on:keypress />
</svelte:component>
