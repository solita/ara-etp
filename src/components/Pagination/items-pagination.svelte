<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';
  import * as PaginationUtils from './pagination-utils';

  export let items;
  export let pageSize = 50;
  export let page = 0;
  export let baseUrl = '#/';
  export let urlFn = page => `#/${page}`;

  $: numberOfPages = Math.ceil(items.length / pageSize);
  $: results = {
    from: Math.min(page * pageSize + 1, items.length),
    to: Math.min(page * pageSize + pageSize, items.length),
    total: items.length
  };
</script>

<style>
  a,
  span {
    @apply block text-dark px-4 py-2 no-underline border-t-1 border-b-1 border-r-1 border-active;
  }

  a.active {
    @apply bg-active text-light;
  }

  a:hover {
    @apply bg-background;
  }

  a:focus {
    @apply outline-none bg-secondary text-light;
  }

  a:first-child {
    @apply rounded-l-lg border-l-1;
  }

  a:last-child {
    @apply rounded-r-lg;
  }
</style>

<!-- purgecss: active -->
<div>
  {#if items.length > 0}
    <slot
      pageItems={R.slice(page * pageSize, page * pageSize + pageSize, items)} />
    <div class="flex w-full mt-4">
      <div class="flex-1 slef-center">
        {$_('pagination.results', { values: results })}
      </div>
      <div class="flex">
        {#if numberOfPages > 0}
          {#if page > 0}
            <a class="flex font-icon" href={`${baseUrl}${urlFn(page - 1)}`}>
              keyboard_arrow_left
            </a>
          {/if}
          {#each R.aperture(2, PaginationUtils.truncate(numberOfPages, page)) as [currentPage, nextPage]}
            <a
              class:active={page === currentPage}
              href={`${baseUrl}${urlFn(currentPage)}`}>{currentPage + 1}</a>
            {#if nextPage - currentPage > 1}<span>...</span>{/if}
          {/each}
          {#if numberOfPages > 1}
            <a
              class:active={page === numberOfPages - 1}
              href={`${baseUrl}${urlFn(numberOfPages - 1)}`}>{numberOfPages}</a>
          {/if}
          {#if page + 1 < numberOfPages}
            <a class="flex font-icon" href={`${baseUrl}${urlFn(page + 1)}`}>
              keyboard_arrow_right
            </a>
          {/if}
        {/if}
      </div>
      <div class="flex-1 self-center" />
    </div>
  {/if}
</div>
