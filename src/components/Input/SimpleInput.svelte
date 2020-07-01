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

  export let viewValue;
  export let rawValue = '';

  export let rawValueAsViewValue = false;

  export let valid = true;

  export let wrapper = SquareInputWrapper;

  let error = false;
  let focused = false;

  $: rawValueAsViewValue && (viewValue = rawValue);

  $: error = focused && !valid;
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
</style>

<!-- purgecss: center error caret search focused disabled-->

<Label {id} {required} {label} {compact} {error} {focused} />
<svelte:component this={wrapper} {caret} {search} {focused} {error} {disabled}>
  <InputField
    {id}
    {name}
    {disabled}
    {center}
    {error}
    {autocomplete}
    {viewValue}
    bind:rawValue
    bind:focused
    on:input />
</svelte:component>
