<script>
  import * as R from 'ramda';

  import * as PaginationUtils from './pagination-utils';
  import * as RamdaUtils from '@Utility/ramda-utils';
  import { _ } from '@Language/i18n';

  export let pageCount = 1;
  export let pageNum = 1;
  export let itemsPerPage = 1;
  export let itemsCount = 1;
  export let nextPageCallback = R.identity();

  const nextPage = next => _ => {
    nextPageCallback(
      R.ifElse(
        RamdaUtils.inRangeInclusive(1, pageCount),
        R.always(next),
        R.always(pageNum)
      )(next)
    );
  };
  $: pagesNear = PaginationUtils.nearForCurrent(pageCount, pageNum);
  $: resultValues = {
    from: R.inc(R.multiply(R.dec(pageNum), itemsPerPage)),
    to: R.add(itemsPerPage, R.multiply(R.dec(pageNum), itemsPerPage)),
    total: itemsCount
  };
</script>

<style type="text/postcss">
  .content {
    @apply flex w-full;
  }
  .pagination {
    @apply flex;
  }

  span,
  button {
    @apply text-dark px-4 py-2 no-underline border-t-1 border-b-1 border-r-1 border-active;
  }

  .results,
  .export {
    @apply flex-1 self-center;
  }
  button.active {
    @apply bg-active text-light;
  }

  .arrow {
    @apply flex;
  }

  button:hover {
    @apply bg-background;
  }

  button:first-child {
    @apply rounded-l-lg border-l-1;
  }

  button:last-child {
    @apply rounded-r-lg;
  }

  button:focus {
    @apply outline-none bg-secondary text-light;
  }
</style>

<div class="content">
  <div class="results">
    {$_('pagination.results', { values: resultValues })}
  </div>
  <div class="pagination">
    <button class="arrow" on:click={nextPage(R.dec(pageNum))}>
      <i class="material-icons">keyboard_arrow_left</i>
    </button>
    <button class={R.equals(1, pageNum) ? 'active' : ''} on:click={nextPage(1)}>
      1
    </button>
    {#if R.head(pagesNear) - 1 > 1}
      <span class="dots">...</span>
    {/if}
    {#each pagesNear as page}
      <button
        class={R.equals(page, pageNum) ? 'active' : ''}
        on:click={nextPage(page)}>
        {page}
      </button>
    {/each}
    {#if pageCount - R.last(pagesNear) > 1}
      <span class="dots">...</span>
    {/if}
    {#if pageCount !== 1}
      <button
        class={R.equals(pageCount, pageNum) ? 'active' : ''}
        on:click={nextPage(pageCount)}>
        {pageCount}
      </button>
    {/if}
    <button class="arrow" on:click={nextPage(R.inc(pageNum))}>
      <i class="material-icons">keyboard_arrow_right</i>
    </button>
  </div>
  <div class="export" />
</div>
