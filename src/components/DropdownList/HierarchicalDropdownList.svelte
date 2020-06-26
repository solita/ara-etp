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
    @apply absolute top-auto left-0 right-0 overflow-auto z-10 shadow-dropdownlist;
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
<ol transition:slide|local={{ duration: 200 }}>
  {#each items as item}
    <HierarchicalNode {onclick} {item} {labelLens} {leafsLens} />
  {/each}
</ol>
