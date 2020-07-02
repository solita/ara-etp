<script>
  export let value = false;
  export let label = '';
  export let toggledIcon = 'expand_less';
  export let notToggledIcon = 'expand_more';
</script>

<style type="text/postcss">
  label {
    @apply text-primary uppercase font-bold cursor-pointer;
  }

  span {
    @apply font-icon text-primary font-bold text-2xl cursor-pointer;
  }
</style>

<div class="flex justify-center items-center">
  <label>
    {label}
    <input bind:checked={value} type="checkbox" class="sr-only" />
  </label>
  <span on:click={_ => (value = !value)}>
    {#if value}{toggledIcon}{:else}{notToggledIcon}{/if}
  </span>
</div>
