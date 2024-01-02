<script>
  import { onMount } from 'svelte';
  import TextButton from '@Component/Button/TextButton';

  export let truncate = true;
  let multiline = false;

  let node;
  let truncated;

  let lineHeight;

  onMount(() => {
    lineHeight = node.children[0].offsetHeight;
    truncated = node.children[0].offsetWidth < node.children[0].scrollWidth;
    multiline = lineHeight < node.children[0].offsetHeight;
  });
</script>

<style>
  div.truncate-children :global(> *) {
    @apply truncate;
  }
</style>

<svelte:window
  on:resize={() => {
    truncated =
      !!node &&
      !!node.children &&
      !!node.children[0] &&
      node.children[0].offsetWidth < node.children[0].scrollWidth;
    multiline =
      !!node &&
      !!node.children &&
      !!node.children[0] &&
      lineHeight < node.children[0].offsetHeight;
  }} />

<div bind:this={node} class="min-w-0" class:truncate-children={truncate}>
  <slot />
</div>

{#if truncated || multiline}
  <div>
    <TextButton
      on:click={_ => {
        truncate = !truncate;
        multiline = !multiline;
      }}
      type={'button'}
      icon={truncate ? 'expand_more' : 'expand_less'}
      text={truncate ? 'Näytä lisää' : 'Näytä vähemmän'} />
  </div>
{/if}
