<script>
  import Header from '@Component/header';
  import Localization, { _ } from '@Localization/localization';
  import Router from '@Router/router';

  import Container, { styles as containerStyles } from '@Component/container';
  import Disclaimer from '@Component/disclaimer';
  import Footer from '@Component/footer';

  const configPromise = fetch('config.json').then(response => response.json());
</script>

<style>
  :global(h1) {
    @apply text-ashblue uppercase font-bold;
  }

  :global(h2) {
    @apply uppercase tracking-widest font-bold;
  }
  @media print {
    #testi-disclaimer {
      display: flex;
      position: fixed;
      justify-content: center;
      align-items: center;
      width: 100%;
      height: 100%;
      z-index: 1000;
      font-size: 14rem;
      opacity: 50%;
      transform: rotate(-45deg);
    }
  }
</style>

<Localization>
  <div class="applicationcontainer flex flex-col font-body font-medium w-full">
    {#await configPromise then config}
      {#if config.showTestEnvNotification}
        <div class="print:hidden">
          <Container {...containerStyles.red}>
            <Disclaimer>{$_('DISCLAIMER_WIP')}</Disclaimer>
          </Container>
        </div>

        <div id="testi-disclaimer" class="hidden">Testi</div>
      {/if}

      <Header {config} />
    {/await}

    <main class="flex-grow">
      <Router />
    </main>
    {#await configPromise then config}
      <Footer {config} />
      {#if config.showTestEnvNotification}
        <div class="sticky bottom-0">
          <Container {...containerStyles.red}>
            <Disclaimer />
          </Container>
        </div>
      {/if}
    {/await}
  </div>
</Localization>
