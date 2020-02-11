<script>
  export let text;
</script>

<style>
  .navigationtab {
    @apply flex flex-grow relative mb-4 py-4 px-8 font-eb justify-center border-dark border-b-2 cursor-pointer uppercase;
  }

  .navigationtab:active::after,
  .navigationtab:hover::after,
  .navigationtab:focus::after {
    content: '';
    @apply bg-hover absolute top-100 left-0 right-0 h-3;
  }

  .navigationtab:focus {
    @apply outline-none;
  }

  .navigationtab:active {
    @apply text-hover;
  }
</style>

<a class="navigationtab" tabindex="0">{text}</a>
