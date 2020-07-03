<script>
  import qs from 'qs';
  import * as R from 'ramda';

  import QueryBlock from './queryblock';

  export let where;

  $: console.log(where);
</script>

<style>
  /* your styles go here */
</style>

<div class="flex flex-col w-full">
  {#each where as or, i}
    {#each or as and, j}
      <QueryBlock
        bind:model={where}
        lens={R.compose( R.lensIndex(i), R.lensIndex(j) )}
        showConjugation={!(i === 0 && j === 0)} />
      <span
        on:click={_ => (where = R.compose( R.filter(R.length), R.over(R.lensIndex(i), R.remove(j, 1)) )(where))}>
        X
      </span>
    {/each}
  {/each}
</div>
