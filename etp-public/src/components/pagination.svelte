<script>
  import { _ } from '@Localization/localization';

  export let page;
  export let pageSize;
  export let itemCount;
  export let currentPageItemCount;
  export let queryStringFn;

  const truncate = (numberOfPages, page) => {
    const pages = [...Array(numberOfPages).keys()];
    const heads = pages.slice(0, 3);
    const lasts = [...pages].reverse().slice(0, 3).reverse();

    const pageNear = pages.slice(page - 2, page + 3);

    const truncatedPages = [...new Set([...heads, ...pageNear, ...lasts])];

    return truncatedPages
      .slice(1)
      .reduce(
        (pages, page) => [
          ...pages,
          ...(page - pages[pages.length - 1] > 1 ? [-1, page] : [page])
        ],
        [truncatedPages[0]]
      );
  };

  $: numberOfPages = Math.ceil(itemCount / pageSize);
</script>

<style>
  .pagination {
    @apply flex mx-auto text-darkgreen text-center font-semibold;
  }

  .pagelink {
    @apply border-darkgrey border-l px-2;
  }
  .pagelink:last-child {
    @apply border-r;
  }
</style>

<!-- purgecss: text-black text-lightgrey -->

<div
  class="flex w-full align-center items-center justify-center my-1 flex-col
  md:flex-row relative">
  <span class="top-0 left-0 md:justify-self-start relative md:absolute">
    {itemCount
      ? `${$_('HAKU_TULOKSET')} ${page * pageSize + 1}...${
          page * pageSize + currentPageItemCount
        }/${itemCount}`
      : ''}
  </span>
  <nav class="pagination w-full md:w-auto select-none">
    {#if numberOfPages > 1}
      <a
        class="uppercase mr-2 hidden md:inline-block"
        href={page > 0 ? queryStringFn(page - 1) : null}
        class:text-darkgrey={page <= 0}>
        {$_('PAGI_EDELLINEN')}
      </a>
      <a
        class="md:hidden px-2 py-2 md:py-0"
        href={page > 0 ? queryStringFn(page - 1) : null}
        class:text-darkgrey={page <= 0}>
        <span class="material-icons" aria-hidden="true"> navigate_before </span>
      </a>

      <div class="cursor-default flex items-center flex-grow">
        {#each truncate(numberOfPages, page) as link}
          {#if link === -1}
            <span class="pagelink uppercase py-2 md:py-0 flex-grow text-black">
              ...
            </span>
          {:else}
            <a
              class="pagelink uppercase py-2 md:py-0 flex-grow"
              class:text-darkgrey={page === link}
              href={page !== link ? queryStringFn(link) : null}>
              {link + 1}
            </a>
          {/if}
        {/each}
      </div>
      <a
        class="uppercase ml-2 hidden md:inline-block"
        href={page + 1 < numberOfPages ? queryStringFn(page + 1) : null}
        class:text-darkgrey={page + 1 >= numberOfPages}>
        {$_('PAGI_SEURAAVA')}
      </a>
      <a
        class="md:hidden px-2 py-2 md:py-0"
        href={page + 1 < numberOfPages ? queryStringFn(page + 1) : null}
        class:text-darkgrey={page + 1 >= numberOfPages}>
        <span class="material-icons" aria-hidden="true"> navigate_next </span>
      </a>
    {/if}
  </nav>
</div>
