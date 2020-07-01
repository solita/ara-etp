<script>
  import Label from '@Component/Label/Label';

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
  let focused = false;

  $: rawValueAsViewValue && (viewValue = rawValue);
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

<!-- purgecss: center error caret search focused disabled-->

<Label {id} {required} {label} {compact} error={focused && !valid} {focused} />
<div
  class="inputwrapper"
  class:caret
  class:search
  class:focused
  class:error={focused && !valid}
  class:disabled>
  <input
    {id}
    {name}
    {disabled}
    class="input"
    class:center
    class:error={focused && !valid}
    type="text"
    {autocomplete}
    value={viewValue}
    on:focus={event => {
      focused = true;
    }}
    on:blur={event => {
      focused = false;
    }}
    on:input={event => (rawValue = event.target.value)}
    on:input />
</div>
