<script>
  export let id;
  export let name;
  export let disabled;
  export let center;
  export let error;
  export let autocomplete = 'off';
  export let viewValue;
  export let type = 'text';
  export let placeholder = '';
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
  on:blur
  on:input
  on:keypress />
