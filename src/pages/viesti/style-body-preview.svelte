<style>
  div.markup :global(ul),
  div.markup :global(ol) {
    margin: 0;
    padding: 0;
    @apply mx-2 whitespace-no-wrap;
  }

  div.markup {
    @apply flex truncate whitespace-no-wrap;
  }

  div.markup :global(li) {
    display: inline;
  }
</style>

<div class="markup"><slot /></div>
