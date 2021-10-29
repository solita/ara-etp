<style>
  div.markup :global(ul),
  div.markup :global(ol) {
    @apply whitespace-no-wrap;
  }

  div.markup :global(li)::before {
    content: '·';
  }

  div.markup {
    @apply flex truncate whitespace-no-wrap;
  }

  div.markup :global(li) {
    @apply mr-1 inline;
  }
</style>

<div class="markup space-x-2"><slot /></div>
