<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  export let items;
  export let pageSize = 5;
  export let page = 0;
  export let baseUrl = '#/';
  export let urlFn = page => `#/${page}`;

  const truncate = (numberOfPages, page) => {
    const pages = R.range(0, numberOfPages);
    const heads = R.slice(0, 2, pages);
    const tails = R.compose(R.reverse, R.slice(0, 2), R.reverse)(pages);

    const nearPages = R.slice(page - 1, page + 2, pages);

    const truncated = R.uniq([...heads, ...nearPages, ...tails]);

    return truncated;
  };

  $: numberOfPages = Math.ceil(items.length / pageSize);
  $: results = {
    from: Math.min(page * pageSize + 1, items.length),
    to: Math.min(page * pageSize + pageSize, items.length),
    total: items.length
  };
</script>

<style>
  a {
    @apply p-4;
  }
</style>

<!-- purgecss: font-bold -->

<div>
  {#if items.length > 0}
    <slot
      pageItems={R.slice(page * pageSize, page * pageSize + pageSize, items)} />
    <div class="flex flex-col mt-4">
      <div>
        {$_('pagination.results', { values: results })}
      </div>
      <div class="flex-grow flex justify-center">
        {#if numberOfPages > 0}
          <div class="w-1/8 flex items-center justify-center">
            {#if page > 0}
              <a
                class="font-icon text-primary text-xl hover:underline"
                href={`${baseUrl}${urlFn(page - 1)}`}>
                chevron_left
              </a>
            {/if}
          </div>
          <div class="w-1/2 flex justify-evenly items-center">
            {#each R.aperture(2, truncate(numberOfPages, page)) as [currentPage, nextPage]}
              <a
                class="text-primary text-xl hover:underline"
                class:font-bold={page === currentPage}
                href={`${baseUrl}${urlFn(currentPage)}`}>{currentPage + 1}</a>
              {#if nextPage - currentPage > 1}<span class="text-2xl">...</span
                >{/if}
            {/each}
            {#if numberOfPages > 1}
              <a
                class="text-primary text-xl hover:underline"
                class:font-bold={page === numberOfPages - 1}
                href={`${baseUrl}${urlFn(numberOfPages - 1)}`}
                >{numberOfPages}</a>
            {/if}
          </div>
          <div class="w-1/8 flex items-center justify-center">
            {#if page + 1 < numberOfPages}
              <a
                class="font-icon text-primary text-xl hover:underline"
                href={`${baseUrl}${urlFn(page + 1)}`}>
                chevron_right
              </a>
            {/if}
          </div>
        {/if}
      </div>
    </div>
  {/if}
</div>
