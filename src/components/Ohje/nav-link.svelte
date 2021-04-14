<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let sivu;
  export let activeSivuId;
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
    <span class="material-icons cursor-default">chevron_right</span>
    <a href={`/#/ohje/${sivu.id}`}>
      <span class="title"> {sivu.title} </span>
    </a>
  </div>
  {#if R.has('children', sivu)}
    <div class="children pl-2 bg-disabled">
      {#each sivu.children as child}
        <svelte:self sivu={child} {activeSivuId} />
      {/each}
    </div>
  {/if}
</div>
