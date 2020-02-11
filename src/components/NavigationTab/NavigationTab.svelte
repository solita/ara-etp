<script>
  export let text;
</script>

<style>
  .navigationtab {
    @apply flex py-4 px-8 font-eb justify-center border-dark border-b-3 cursor-pointer uppercase shadow-none;
    transition: box-shadow 0.1s ease-in-out;
  }

  .navigationtab:active,
  .navigationtab:focus,
  .navigationtab:hover {
    @apply shadow-hover-2 border-hover;
  }

  .navigationtab:focus {
    @apply outline-none;
  }

  .navigationtab:active {
    @apply text-hover;
  }
</style>

<a class="navigationtab" tabindex="0">{text}</a>
