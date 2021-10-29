<style>
  div.markup :global(ul),
  div.markup :global(ol) {
    list-style: revert;
    margin: 0;
    padding: 0;
    white-space: normal;
    @apply mx-2;
  }

  div.markup {
    @apply flex truncate;
  }

  div.markup :global(li) {
    display: inline;
  }
</style>

<div class="markup"><slot /></div>
