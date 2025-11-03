<script>
  import * as R from 'ramda';

  export let item;
  export let labelLens;
  export let leafsLens;
  export let margin = 1.5;
  export let onclick = () => {};

  $: label = R.view(labelLens, item);
  $: leafs = R.defaultTo([], R.view(leafsLens, item));
</script>

<style type="text/postcss">
  .nodeitem {
    @apply cursor-pointer bg-light py-4;
  }

  li:not(:first-child) {
    @apply border-t-1 border-disabled;
  }

  .nodeitem:not(.header):hover {
    @apply bg-background text-dark;
  }

  .active {
    @apply bg-primary text-light outline-none;
  }

  .subitem {
    @apply py-0;
  }

  .header {
    @apply cursor-default font-bold;
  }
</style>

<li
  style="padding-left:{margin}em;"
  class="nodeitem dropdownitem"
  class:header={leafs.length > 0}
  on:click={_ => onclick(item)}>
  {label}
</li>

{#if leafs.length > 0}
  <li class="subitem">
    {#each leafs as leaf}
      <svelte:self
        {onclick}
        margin={margin + 0.5}
        {labelLens}
        {leafsLens}
        item={leaf} />
    {/each}
  </li>
{/if}
