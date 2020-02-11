<script>
  export let text;
</script>

<style>
  .navigationtab {
    @apply flex flex-grow relative mb-4 py-4 px-8 font-eb justify-center border-dark border-b-2 cursor-pointer uppercase;
  }

  .navigationtab:active {
    @apply text-hover;
  }

  .navigationtab:active::after {
    content: '';
    @apply bg-hover absolute top-100 left-0 right-0 h-3;
  }

  .navigationtab:hover {
    @apply border-hover;
  }

  .navigationtab:hover::after {
    content: '';
    @apply bg-hover absolute top-100 left-0 right-0 h-3;
  }
</style>

<a class="navigationtab">{text}</a>
