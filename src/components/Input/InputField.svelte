<script>
  import * as keys from '@Utility/keys';

  export let id;
  export let name;
  export let disabled;
  export let center;
  export let error;
  export let autocomplete = 'off';
  export let viewValue;
  export let type = 'text';
  export let placeholder = '';
  export let api;
</script>

<style type="text/postcss">
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
</style>

<!-- purgecss: center error -->
<input
  {id}
  {name}
  {disabled}
  data-cy={name}
  class="input"
  class:center
  class:error
  {type}
  {autocomplete}
  {placeholder}
  value={viewValue}
  on:focus={api.focus}
  on:blur={event => api.blur(event.target.value)}
  on:input={event => api.input(event.target.value)}
  on:keydown={event => {
    if (event.keyCode === keys.ENTER) {
      api.update(event.target.value);
    }
  }} />
