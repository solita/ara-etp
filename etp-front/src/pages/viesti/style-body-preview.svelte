<style>
  div.markup :global(ul),
  div.markup :global(ol) {
    @apply whitespace-nowrap;
  }

  div.markup :global(li)::before {
    content: '\2022';
  }

  div.markup {
    @apply flex truncate whitespace-nowrap;
  }

  div.markup :global(li) {
    @apply mr-1 inline;
  }
</style>

<div class="markup space-x-2"><slot /></div>
