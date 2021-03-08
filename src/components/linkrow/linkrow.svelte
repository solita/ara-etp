<script>
  export let contents = [];
  export let href = '#';
</script>

{#each contents as content}
  <td class="etp-table--td">
    <a class="block" {href}>
      {content}
    </a>
  </td>
{/each}
