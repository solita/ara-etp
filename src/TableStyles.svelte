<style type="text/postcss">
  :global(.etp-table) {
    @apply w-full text-sm table-auto;
  }

  :global(.etp-table--thead) {
    @apply text-primary uppercase text-left tracking-wider bg-light;
  }

  :global(.etp-table--th),
  :global(.etp-table--td) {
    @apply px-2 py-1;
  }

  :global(.etp-table--td:not(:first-child)),
  :global(.etp-table--th:not(:first-child)) {
    @apply border-disabled border-l-1;
  }

  :global(.etp-table--tbody > :nth-child(odd)) {
    @apply bg-background;
  }

  :global(.etp-table--tr__link:hover) {
    @apply cursor-pointer bg-althover;
  }

  :global(.etp-table--td__center),
  :global(.etp-table--th__center) {
    @apply text-center;
  }
</style>
