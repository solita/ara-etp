<script>
  export let href;
  export let text;
</script>

<style type="text/postcss">
  a {
    @apply text-link;
  }

  a:hover {
    @apply underline;
  }
</style>

<a href="{href}">{text}</a>
