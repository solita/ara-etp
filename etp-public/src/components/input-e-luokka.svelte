<script>
  import { _ } from '@Localization/localization';
  const luokat = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

  export let group = [];
  export let name;
</script>

<style>
  .checkbox-container input:focus ~ .checkbox-text {
    @apply underline;
  }
  .checkbox-container input:focus ~ .material-icons {
    @apply text-green;
  }
  .checkbox-container input {
    @apply absolute opacity-0 cursor-pointer select-none outline-none pointer-events-none;
  }
  .checkbox-container .material-icons {
    @apply select-none text-4xl;
  }
  .checkbox-container input:checked ~ .checked,
  .checkbox-container input ~ .unchecked {
    @apply inline-block;
  }
  .checkbox-container input ~ .checked,
  .checkbox-container input:checked ~ .unchecked {
    @apply hidden;
  }
</style>

<fieldset class="flex flex-row flex-wrap sm:justify-between items-center">
  <legend class="sr-only">{$_('ET_LUOKKA')}:</legend>
  {#each luokat as luokka}
    <label class="checkbox-container flex items-center px-3 py-2 md:p-0">
      <input on:change {name} type="checkbox" bind:group value={luokka} />
      <span class="material-icons checked text-green" aria-hidden="true">
        check_box
      </span>
      <span class="material-icons unchecked" aria-hidden="true">
        check_box_outline_blank
      </span>
      <span class="ml-1 checkbox-text">{luokka}</span>
    </label>
  {/each}
</fieldset>
