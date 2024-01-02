<style>
  div.markup :global(.ql-container) {
    font-size: 1em;
  }

  div.markup :global(h1),
  div.markup :global(h2),
  div.markup :global(h3) {
    color: #2e5053;
    font-weight: 700;
    margin: 0;
  }

  div.markup :global(h1) {
    font-size: 1.25rem;
    letter-spacing: 0.15em;
  }

  div.markup :global(h2) {
    font-size: 1.125em;
  }

  div.markup :global(h3) {
    font-size: 1.125;
    font-weight: normal;
    letter-spacing: 0.15em;
  }

  div.markup :global(a) {
    position: relative;
    color: #538000;
    border-bottom-width: 1px;
    border-color: transparent;
  }

  div.markup :global(a:hover) {
    cursor: pointer;
    border-color: #538000;
  }
  div.markup :global(ul),
  div.markup :global(ol) {
    list-style: revert;
    margin: 0;
    padding: revert;
    white-space: normal;
  }
</style>

<div class="markup"><slot /></div>
