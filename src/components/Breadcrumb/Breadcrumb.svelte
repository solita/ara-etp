<script>
  import Link from '@Component/Link/Link';
  export let value;
</script>

<style type="text/postcss">
  div {
    @apply bg-background px-20 flex h-16 items-center text-primary;
  }
</style>

<div>
  <Link href="/" icon="home" />
  {#each value as {label, url}, i (url)}
    <span>&nbsp;/&nbsp</span>
    <Link href="{url}" text="{label}" />
  {/each}
</div>
