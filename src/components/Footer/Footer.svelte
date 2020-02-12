<script>

</script>

<style>
  footer {
    @apply flex flex-col w-full;
  }

  .adminmessage {
    @apply bg-secondary text-light h-64;
  }

  .info {
    @apply flex bg-background h-64;
  }

  .info > * {
    @apply px-2 items-stretch w-1/4;
  }

  .arafooter {
    @apply flex justify-end items-end w-1/2;
  }

  .arafooter {
    @apply text-primary stroke-current fill-current;
  }
</style>

<footer>
  <section class="adminmessage">Viesti pääkäyttäjälle</section>
  <section class="info">
    <div class="guidance" />
    <div class="contact" />
    <div class="arafooter">
      <img class="araprimary" src="images/ara_logo_simple_nega.svg" alt="Ara" />
    </div>
  </section>
</footer>
