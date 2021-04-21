<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { slide } from 'svelte/transition';

  export let sivu;
  export let activeSivuId = -1;
  export let childrenShown = true;
</script>

<style>
  .root {
    @apply bg-secondary text-light border-light;
  }
  .child {
    @apply bg-light text-dark border-secondary;
  }
  .active.root {
    @apply bg-dark;
  }
  .active.child {
    @apply bg-althover;
  }
  .active .title {
    @apply font-bold;
  }
  .material-icons {
    @apply select-none px-1;
  }
  .root .text-error {
    @apply text-light;
  }

  .root ~ .children {
    @apply border-b border-light;
  }
</style>

<div class="flex flex-col w-full">
  <div
    class="flex w-full border-b items-center"
    class:active={sivu.id === parseInt(activeSivuId)}
    class:root={sivu['parent-id'].isNone()}
    class:child={sivu['parent-id'].isSome()}>
    {#if !R.isEmpty(sivu.children)}
      <button
        class="p-2"
        on:click={() => {
          childrenShown = !childrenShown;
        }}>
        {#if childrenShown}
          <span class="material-icons">expand_more</span>
        {:else}
          <span class="material-icons">chevron_right</span>
        {/if}
      </button>
    {:else}
      <span class="material-icons text-transparent">circle</span>
    {/if}
    <a href={`/#/ohje/${sivu.id}`} class="flex-grow p-2 items-center">
      <span class="title"> {sivu.title} </span>
      {#if !sivu.published}
        <span class="font-icon text-error"> visibility_off </span>
      {/if}
    </a>
  </div>
  {#if !R.isEmpty(sivu.children) && childrenShown}
    <div
      class="children pl-1 bg-secondary"
      transition:slide|local={{ duration: 100 }}>
      {#each sivu.children as child}
        <svelte:self sivu={child} {activeSivuId} />
      {/each}
    </div>
  {/if}
</div>
