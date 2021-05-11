<style>
  div.markup :global(.ql-container) {
    font-size: 1em !important;
  }

  div.markup :global(h1),
  div.markup :global(h2),
  div.markup :global(h3) {
    font-weight: 700 !important;
    text-transform: uppercase !important;
  }

  div.markup :global(h1) {
    color: #2e5053 !important;
    font-size: 1.125em !important;
    margin-bottom: 2rem !important;
    letter-spacing: 0.15em !important;
  }

  div.markup :global(h2) {
    color: #538000 !important;
    font-size: 1rem !important;
    margin-bottom: 1rem !important;
  }

  div.markup :global(h3) {
    color: #fff !important;
    width: 100% !important;
    background-color: #2e5053 !important;
    font-size: 0.875rem !important;
    margin-top: 0 !important;
    margin-bottom: 1rem !important;
    padding: 0.75rem !important;
    letter-spacing: 0.15em !important;
  }

  div.markup :global(a) {
    position: relative !important;
    color: #538000 !important;
    border-bottom-width: 1px !important;
    border-color: transparent !important;
  }

  div.markup :global(a:hover) {
    cursor: pointer !important;
    border-color: #538000 !important;
  }
  div.markup :global(ul),
  div.markup :global(ol) {
    list-style: revert;
    margin: revert;
    padding: revert;
  }
</style>

<div class="markup"><slot /></div>
