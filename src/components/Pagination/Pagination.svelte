<script>
  import * as R from 'ramda';

  export let pageCount;
  export let pageNum;

  const isPageCenterItem = items => {
    return R.equals(pageNum, R.nth(1, items));
  };

  const addNearByDots = (acc, item) => {
    const last = R.last(acc);
    const isNotNextItem = R.gt(R.subtract(item, last), 1);
    return isNotNextItem ? R.append(['x', item], acc) : R.append(item, acc);
  };

  const getNextItems = R.always(
    R.ifElse(
      R.equals(1),
      R.always([R.inc(pageNum)]),
      R.always([R.dec(pageNum)])
    )(pageNum)
  );

  const indexes = R.compose(
    R.flatten,
    R.reduce(addNearByDots, []),
    R.sort((a, b) => a - b),
    R.union([1, pageCount]),
    R.flatten,
    R.when(R.isEmpty, getNextItems),
    R.filter(isPageCenterItem),
    R.aperture(3),
    R.range(1),
    R.inc
  )(pageCount);
</script>

<style type="text/postcss">
  .pagination {
    @apply flex;
  }

  .dots,
  .pagination a {
    @apply text-dark px-4 py-2 no-underline border-1 border-active;
  }

  .pagination a.active {
    @apply bg-active;
  }

  .pagination a:hover:not(.active) {
    @apply bg-hover;
  }

  .pagination a:first-child {
    @apply rounded-l-lg;
  }

  .pagination a:last-child {
    @apply rounded-r-lg;
  }
</style>

{#if pageCount > 0}
  <div class="pagination">
    <a href="#">&laquo;</a>

    {#each indexes as index}
      {#if R.equals(index, 'x')}
        <span class="dots">...</span>
      {:else}
        <a class={R.equals(index, pageNum) ? 'active' : ''} href="#">{index}</a>
      {/if}
    {/each}

    <a href="#">&raquo;</a>
  </div>
{/if}
