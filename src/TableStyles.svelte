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

  :global(.etp-table--tr > *:not(:first-child)) {
    @apply border-disabled border-l-1;
  }

  :global(.etp-table--tbody .etp-table--tr:nth-child(odd)) {
    @apply bg-background;
  }
</style>
