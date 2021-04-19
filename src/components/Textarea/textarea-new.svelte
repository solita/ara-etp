<script>
  import { autoresize } from './autoresize';

  export let id;
  export let name = '';
  export let required = false;
  export let disabled = false;
  export let autocomplete = 'off';
  export let viewValue = '';

  export let min = 10;
  export let max = 30;
</script>

<style>
  textarea:focus {
    outline: none;
  }
</style>

<textarea
  class="focus:outline-none"
  {id}
  {name}
  data-cy={name}
  {disabled}
  {autocomplete}
  use:autoresize={[min, max]}
  value={viewValue}
  on:keydown
  on:keypress />
