<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  export let items = [];
  export let active = Maybe.None();
  export let component = null;
  export let onclick;

  let listElement;
  let previousIndex;

  const checkIfInView = (container, element, partial) => {
    const containerTop = container.scrollTop;
    const containerBottom = containerTop + container.clientHeight;

    const elementTop = element.offsetTop;
    const elementBottom = elementTop + element.clientHeight;

    const isTotal =
      elementTop >= containerTop && elementBottom <= containerBottom;
    const isPartial =
      partial &&
      ((elementTop < containerTop && elementBottom > containerTop) ||
        (elementBottom > containerBottom && elementTop < containerBottom));

    return isTotal || isPartial;
  };

  const scrollIndexToView = index => {
    if (!listElement) {
      previousIndex = 0;
    }
    if (
      listElement &&
      listElement.children[index] &&
      !checkIfInView(listElement, listElement.children[index], false)
    ) {
      listElement.children[index].scrollIntoView(previousIndex > index);
    }
    previousIndex = index;
  };

  $: R.forEach(scrollIndexToView, active);
</script>

<style type="text/postcss">
  ol {
    @apply absolute top-auto left-0 right-0 overflow-auto z-10 shadow-dropdownlist;
    max-height: 35.6em;
  }

  ol::-webkit-scrollbar {
    @apply w-2;
  }

  ol::-webkit-scrollbar-track {
    @apply bg-background;
  }

  ol::-webkit-scrollbar-thumb {
    @apply bg-disabled;
  }

  ol::-webkit-scrollbar-thumb:hover {
    @apply bg-dark;
  }

  li {
    @apply py-4 pl-6 cursor-pointer bg-light;
  }

  li:not(:first-child) {
    @apply border-disabled border-t-1;
  }

  li:hover {
    @apply bg-background text-dark;
  }

  .active {
    @apply outline-none bg-primary text-light;
  }
</style>

<!-- purgecss: active -->
<ol
  bind:this={listElement}
  transition:slide|local={{ duration: 200 }}
  on:introend={() => scrollIndexToView(Maybe.orSome(0, active))}>
  {#each items as item, index}
    <li
      class="dropdownitem"
      class:active={Maybe.fold(false, R.equals(index), active)}
      on:click={_ => onclick(item, index)}>
      {#if component}
        <svelte:component this={component} state={item} />
      {:else}{item}{/if}
    </li>
  {/each}
</ol>
