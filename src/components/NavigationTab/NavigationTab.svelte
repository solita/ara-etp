<script>
  export let text;
  export let href;
</script>

<style type="text/postcss">
  a {
    @apply flex py-4 px-8 font-extrabold justify-center border-dark border-b-3 cursor-pointer uppercase shadow-none;
    transition: box-shadow 0.1s ease-in-out;
  }

  a:active,
  a:focus,
  a:hover {
    @apply shadow-hover-2 border-hover;
  }

  a:hover {
    @apply bg-background;
  }

  a:focus {
    @apply outline-none;
  }

  a:active {
    @apply text-hover;
  }
</style>

<a {href} class="navigationtab" tabindex="0">{text}</a>
