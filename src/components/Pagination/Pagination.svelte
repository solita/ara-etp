<script>
  import * as R from 'ramda';

  import * as PaginationUtils from './pagination-utils';

  export let pageCount = 1;
  export let pageNum = 1;

  const pagesNear = PaginationUtils.nearForCurrent(pageCount, pageNum);
</script>

<style type="text/postcss">
  div {
    @apply flex items-center;
  }

  span,
  a {
    @apply text-dark px-4 py-2 no-underline border-t-1 border-b-1 border-r-1 border-active;
  }

  a.active {
    @apply bg-active;
  }

  .arrow {
    @apply flex;
  }

  a:hover:not(.active) {
    @apply bg-hover;
  }

  a:first-child {
    @apply rounded-l-lg border-l-1;
  }

  a:last-child {
    @apply rounded-r-lg;
  }
</style>

{#if pageCount > 0}
  <div class="pagination">
    <a class="arrow" href="#">
      <i class="material-icons">keyboard_arrow_left</i>
    </a>
    <a class={R.equals(1, pageNum) ? 'active' : ''} href="#">1</a>
    {#if R.head(pagesNear) - 1 > 1}
      <span class="dots">...</span>
    {/if}
    {#each pagesNear as page}
      <a class={R.equals(page, pageNum) ? 'active' : ''} href="#">{page}</a>
    {/each}
    {#if pageCount - R.last(pagesNear) > 1}
      <span class="dots">...</span>
    {/if}
    {#if pageCount !== 1}
      <a class={R.equals(pageCount, pageNum) ? 'active' : ''} href="#">
        {pageCount}
      </a>
    {/if}
    <a class="arrow" href="#">
      <i class="material-icons">keyboard_arrow_right</i>
    </a>
  </div>
{/if}
