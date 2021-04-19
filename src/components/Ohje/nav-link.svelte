<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import Autocomplete from '../Autocomplete/Autocomplete.svelte';

  export let sivu;
  export let activeSivuId;

  let expanded = true;
</script>

<style>
  .root {
    @apply bg-secondary text-light border-light;
  }
  .child {
    @apply bg-light text-dark border-secondary;
  }
  .active .title {
    @apply font-bold underline;
  }
</style>

<div class="flex flex-col w-full">
  <div
    class="flex w-full px-1 py-2 border-b"
    class:active={sivu.id == activeSivuId}
    class:root={sivu['parent-id'].isNone()}
    class:child={sivu['parent-id'].isSome()}>
    {#if R.has('children', sivu)}
      <button
        class="flex-shrink"
        on:click={() => {
          expanded = !expanded;
        }}>
        {#if expanded}
          <span class="material-icons"> expand_more </span>
        {:else}
          <span class="material-icons">chevron_right</span>
        {/if}
      </button>
    {:else}<span class="material-icons text-transparent"> circle </span>
    {/if}
    <a href={`/#/ohje/${sivu.id}`} class="flex-grow">
      <span class="title"> {sivu.title} </span>
    </a>
  </div>
  {#if R.has('children', sivu) && expanded}
    <div class="children pl-2 bg-disabled">
      {#each sivu.children as child}
        <svelte:self sivu={child} {activeSivuId} />
      {/each}
    </div>
  {/if}
</div>
