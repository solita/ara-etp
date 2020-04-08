<script>
  export let href;
  export let text = '';
  export let icon;
</script>

<style type="text/postcss">
  a {
    @apply text-link flex;
  }

  a:hover {
    @apply underline;
  }

  span {
    @apply mr-1;
  }
</style>

<a {href}>
  {#if icon}
    <span class="font-icon">{icon}</span>
  {/if}

  {#if text}{text}{/if}
</a>
