<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { dragdrop } from './dragdrop';
  import { slide } from 'svelte/transition';

  export let sivu;
  export let activeSivuId = -1;
  export let childrenShown = true;
  export let draggable = false;
  export let updateSivu;

  let isBeingDragged = false;
  let isBeingTargeted = false;
  let setDroppedAsChild = false;

  const hoverTime = 1000;
  let hoverTimeout;

  // when dragged
  const dragStart = e => {
    if (!draggable) return;

    isBeingDragged = true;
    e.dataTransfer.setData('text/plain', sivu.id);
  };

  const dragEnd = e => {
    if (!draggable) return;
    isBeingDragged = false;
  };

  // when targeted
  const dragEnter = e => {
    if (!draggable) return;
    e.preventDefault();
    isBeingTargeted = true;
    setDroppedAsChild = false;
    hoverTimeout = setTimeout(() => {
      setDroppedAsChild = true;
    }, hoverTime);
  };
  const dragLeave = e => {
    if (!draggable) return;
    isBeingTargeted = false;
    clearTimeout(hoverTimeout);
  };

  const drop = e => {
    if (!draggable) return;
    isBeingTargeted = false;

    const droppedSivuId = parseInt(e.dataTransfer.getData('text/plain'));
    const targetSivuId = parseInt(sivu.id);

    if (droppedSivuId === targetSivuId) return;

    if (setDroppedAsChild) {
      // set dropped as target's child
      updateSivu(droppedSivuId, {
        'parent-id': Maybe.Some(sivu.id),
        ordinal: 0 // to do this right we would need to know the target's children's lowest or highest ordinal
      });
    } else {
      // set dropped as target's sibling
      updateSivu(droppedSivuId, {
        'parent-id': sivu['parent-id'],
        ordinal: parseInt(sivu.ordinal) + 1 // for this to work right, we need to manage sivu ordinals better, preferably in the back end
      });
    }
  };
</script>

<style type="text/postcss">
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
    @apply font-semibold;
  }
  .material-icons {
    @apply select-none;
  }
  .root .text-dark {
    @apply text-light;
  }

  .root ~ .children {
    @apply border-b border-light;
  }
  .draggable {
    @apply cursor-move;
  }

  .draggable a {
    @apply pointer-events-none font-hairline select-none;
  }

  .isBeingDragged {
    @apply opacity-50;
  }
  .isBeingTargeted.hasNoChildren:not(.setDroppedAsChild),
  .isBeingTargeted:not(.childrenShown):not(.setDroppedAsChild),
  .isBeingTargeted:not(.setDroppedAsChild) ~ .children {
    @apply border-b-8 border-hover;
  }
  .isBeingTargeted.setDroppedAsChild.hasNoChildren,
  .isBeingTargeted:not(.childrenShown).setDroppedAsChild {
    @apply border-b-8 border-warning;
  }
  .isBeingTargeted.setDroppedAsChild ~ .children {
    @apply border-t-8 border-warning;
  }
</style>

<div class="flex flex-col w-full">
  <div
    use:dragdrop={{ dragStart, dragEnd, dragEnter, dragLeave, drop }}
    id={sivu.id}
    class="flex w-full border-b items-center"
    class:active={sivu.id === parseInt(activeSivuId)}
    class:root={sivu['parent-id'].isNone()}
    class:child={sivu['parent-id'].isSome()}
    class:hasNoChildren={R.isEmpty(sivu.children)}
    class:draggable
    class:isBeingDragged
    class:isBeingTargeted
    class:setDroppedAsChild
    class:childrenShown
    {draggable}>
    {#if !R.isEmpty(sivu.children)}
      <button
        class="p-2 focus:outline-none focus:underline"
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
      <div class="text-transparent p-2 pointer-events-none">
        <span class="material-icons">circle</span>
      </div>
    {/if}
    <a href={`/#/ohje/${sivu.id}`} class="flex-grow p-2 items-center">
      <span class="title"> {sivu.title} </span>
      {#if !sivu.published}
        <span class="font-icon text-dark"> visibility_off </span>
      {/if}
    </a>
    {#if draggable}
      <div class="p-2">
        <span class="material-icons"> drag_handle </span>
      </div>
    {/if}
  </div>
  {#if !R.isEmpty(sivu.children) && childrenShown}
    <div
      class="children pl-1 bg-secondary"
      transition:slide|local={{ duration: 100 }}>
      {#each sivu.children as child}
        <svelte:self sivu={child} {activeSivuId} {draggable} {updateSivu} />
      {/each}
    </div>
  {/if}
</div>
