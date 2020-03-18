<script>
  import * as R from 'ramda';
  import Pagination from './../Pagination/Pagination';
  import TableColumnAction from './TableColumnAction';
  import TableColumnValidation from './TableColumnValidation';
  export let fields = [];
  export let tablecontents = [];
  export let validate = {};
  export let pageCount = 0;
  export let pageNum = 0;
</script>

<style type="text/postcss">
  table {
    @apply w-full;
  }

  th {
    @apply px-4 py-2 text-center;
  }

  tr {
    @apply px-4 py-2;
  }

  td {
    @apply text-center;
  }

  tr:nth-child(even) {
    @apply bg-background;
  }

  .tablecontainer {
    @apply flex flex-col items-center;
  }

  .pagination:not(empty) {
    @apply mt-4;
  }
</style>

<div class="tablecontainer">
  <table>
    <thead>
      <tr>
        {#each fields as field}
          <th>
            {#if field.title}{field.title}{/if}
            {#if R.equals(field.sort, 'ascend')}
              <span class="font-icon">keyboard_arrow_down</span>
            {/if}
            {#if R.equals(field.sort, 'descend')}
              <span class="font-icon">keyboard_arrow_up</span>
            {/if}
          </th>
        {/each}
      </tr>
    </thead>
    <tbody>
      {#each tablecontents as row, index}
        <tr>
          {#each fields as field}
            <td>
              {#if R.equals(field.type, 'action')}
                <svelte:component
                  this={TableColumnAction}
                  actions={field.actions}
                  {index} />
              {:else}
                <svelte:component
                  this={TableColumnValidation}
                  value={R.prop(field.id, row)}
                  validation={R.prop(field.id, validate)}
                  component={field.component} />
              {/if}
            </td>
          {/each}
        </tr>
      {/each}
    </tbody>
  </table>

  {#if R.gt(pageCount, 1)}
    <div class="pagination">
      <Pagination {pageCount} {pageNum} />
    </div>
  {/if}
</div>
