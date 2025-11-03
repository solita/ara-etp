<script>
  import * as R from 'ramda';
  import HierarchicalNode from './HierarchicalNode';

  export let items;
  export let labelLens;
  export let leafsLens;
  export let onclick = () => {};
</script>

<style>
  ol {
    @apply absolute left-0 right-0 top-auto z-10 overflow-auto shadow-dropdownlist;
    max-height: 17.8em;
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
    @apply cursor-pointer bg-light py-4 pl-6;
  }

  li:not(:first-child) {
    @apply border-t-1 border-disabled;
  }

  li:hover {
    @apply bg-background text-dark;
  }

  .active {
    @apply bg-primary text-light outline-none;
  }
</style>

<ol transition:slide={{ duration: 200 }}>
  {#each items as item}
    <HierarchicalNode {onclick} {item} {labelLens} {leafsLens} />
  {/each}
</ol>
