<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { dragdrop } from './dragdrop';
  import { slide } from 'svelte/transition';
  import { _ } from '@Language/i18n';

  export let sivu;
  export let activeSivuId = -1;
  export let childrenShown = true;
  export let draggable = false;
  export let updateSivu;

  let isBeingDragged = false;
  let isBeingTargeted = false;
  let setDroppedAsChild = false;

  const i18n = $_;
  const hoverTime = 1000;
  let hoverTimeout;

  // events when NavLink is being dragged
  const dragStart = e => {
    if (!draggable) return;
    isBeingDragged = true;
    e.dataTransfer.setData('text/plain', sivu.id);
  };
  const dragEnd = e => {
    if (!draggable) return;
    isBeingDragged = false;
  };

  // events when NavLink is being targeted
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
      updateSivu(droppedSivuId, {
        'parent-id': Maybe.Some(sivu.id),
        ordinal: 0 // set to null if wanted as the last child
      });
    } else {
      updateSivu(droppedSivuId, {
        'parent-id': sivu['parent-id'],
        ordinal: parseInt(sivu.ordinal) + 1
      });
    }
  };
</script>

<style type="text/postcss">
  .root .link-content,
  .root ~ .destination {
    @apply bg-secondary text-light border-light;
  }
  .child .link-content,
  .children .destination {
    @apply bg-light text-dark border-secondary;
  }
  .active.root .link-content {
    @apply bg-primary;
  }
  .active.child .link-content {
    @apply bg-althover;
  }
  .active .link-content .title {
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
</style>

<div class="flex flex-col w-full">
  <div
    use:dragdrop={{ dragStart, dragEnd, dragEnter, dragLeave, drop }}
    id={sivu.id}
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
    <div class="link-content flex w-full border-b items-center">
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
        <div class="p-2 pointer-events-none">
          <span class="material-icons"> drag_handle </span>
        </div>
      {/if}
    </div>
  </div>
  {#if (!R.isEmpty(sivu.children) && childrenShown) || (draggable && isBeingTargeted && setDroppedAsChild)}
    <div
      class="children pl-1 bg-secondary"
      transition:slide|local={{ duration: 100 }}>
      {#if draggable && isBeingTargeted && setDroppedAsChild}
        <div
          class="destination flex w-full border-b items-center pointer-events-none">
          <span class="material-icons p-2"> subdirectory_arrow_right </span>
          <span class="flex-grow p-2 items-center font-semibold">
            {i18n('ohje.navigation.destination-child')}
          </span>
        </div>
      {/if}
      {#each sivu.children as child}
        <svelte:self sivu={child} {activeSivuId} {draggable} {updateSivu} />
      {/each}
    </div>
  {/if}

  {#if draggable && isBeingTargeted && !setDroppedAsChild}
    <div
      class="destination flex w-full border-b items-center pointer-events-none">
      <span class="material-icons p-2"> east </span>
      <span class="flex-grow p-2 items-center font-semibold">
        {i18n('ohje.navigation.destination-sibling')}
      </span>
    </div>
  {/if}
</div>
