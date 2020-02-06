<script>
  import { customClick } from './input';

  export let value = '';
  export let label = '';
  export let error = false;
  export let caret = false;
  export let id = '';
  export let required = false;

  let focused = false;
</script>

<style>
  label {
    @apply text-secondary lowercase text-xs;
  }

  label.required::before {
    @apply font-icon text-xs;
    content: '*';
  }

  .inputwrapper {
    @apply flex items-center border-b-3 border-disabled text-dark;
  }

  input {
    @apply flex-grow relative font-bold py-1;
  }

  .caret::after {
    @apply font-icon absolute text-base text-disabled;
    right: 0.5em;
    content: 'expand_more';
  }

  .input:focus {
    @apply outline-none;
  }

  .error {
    @apply border-error;
  }

  .focused {
    @apply border-primary;
  }

  .focused::after {
    @apply text-primary;
  }
</style>

<label for={id} class:required>{label}</label>

<div class="inputwrapper" class:caret class:focused class:error>
  <input
    {id}
    {required}
    on:focus={_ => (focused = true)}
    on:focus
    on:blur={_ => (focused = false)}
    on:blur
    class="input"
    class:error
    type="text"
    {value} />
</div>
