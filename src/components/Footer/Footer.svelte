<script>
  import Version from '@Component/Version/Version';
</script>

<style type="text/postcss">
  footer {
    @apply flex flex-col;
  }

  footer > * {
    @apply px-20;
  }

  .adminmessage {
    @apply bg-secondary text-light h-64 font-extrabold uppercase;
  }

  .info {
    @apply flex bg-background h-64 items-stretch;
  }

  .info > * {
    @apply w-1/4;
  }

  .arafooter {
    @apply flex justify-end items-end w-1/2;
  }

  .version {
    @apply flex items-end;
  }
</style>

<footer>
  <section class="info">
    <div class="version">
      <Version />
    </div>
    <div class="contact" />
    <div class="arafooter">
      <img class="araprimary" src="images/ara_logo_simple_nega.svg" alt="Ara" />
    </div>
  </section>
</footer>
