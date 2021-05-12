<style>
  div.markup :global(.ql-container) {
    font-size: 1em;
  }

  div.markup :global(h1),
  div.markup :global(h2),
  div.markup :global(h3) {
    font-weight: 700;
    text-transform: uppercase;
  }

  div.markup :global(h1) {
    color: #2e5053;
    font-size: 1.125em;
    margin-bottom: 2rem;
    letter-spacing: 0.15em;
  }

  div.markup :global(h2) {
    color: #538000;
    font-size: 1rem;
    margin-bottom: 1rem;
  }

  div.markup :global(h3) {
    color: #fff;
    width: 100%;
    background-color: #2e5053;
    font-size: 0.875rem;
    margin-top: 0;
    margin-bottom: 1rem;
    padding: 0.75rem;
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
    margin: revert;
    padding: revert;
  }
</style>

<div class="markup"><slot /></div>
