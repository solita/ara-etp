<script>
  export let id;
  export let name;
  export let disabled;
  export let center;
  export let error;
  export let autocomplete = 'off';
  export let viewValue;
  export let rawValue;
  export let type = 'text';

  export let focused;
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
  class="input"
  class:center
  class:error
  {type}
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
