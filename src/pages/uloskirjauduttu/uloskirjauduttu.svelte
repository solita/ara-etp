<script>
  import { _ } from '@Language/i18n';
  import * as Maybe from '@Utility/maybe-utils';
  import Footer from '@Pages/footer/Footer';
  import LanguageSelect from '@Pages/header/language-select';
  import Link from '@Component/Link/Link';

  import * as versionApi from '@Component/Version/version-api';
  import * as Future from '@Utility/future-utils';

  let resources = Maybe.None();
  Future.fork(
    () => {},
    response => {
      resources = Maybe.Some(response);
    },
    Future.parallelObject(2, {
      version: versionApi.getVersion,
      config: versionApi.getConfig
    })
  );
</script>

<style type="text/postcss">
  .headercontainer {
    @apply bg-secondary;
  }

  header {
    @apply flex px-10 h-20 uppercase text-light justify-between items-center font-bold tracking-xl;
  }

  .footercontainer {
    @apply w-full flex justify-center bg-background;
  }
</style>

<div class="headercontainer">
  <div class="w-full max-w-1440 mx-auto">
    <header class="flex justify-between">
      <div class="flex flex-1 items-center">
        <a href="/">
          <img class="mr-8" src="images/ara_logo_simple_nega.svg" alt="Ara" />
        </a>
        <LanguageSelect />
      </div>
    </header>
  </div>
</div>

<div class="flex flex-col justify-center items-center text-3xl">
  <div>
    <span class="font-semibold text-primary">{$_('log-out-done.message')}</span>
    {#each Maybe.toArray(resources) as { config }}
      <Link
        bold={false}
        href={config.publicSiteUrl}
        text={$_('log-out-done.link')} />
    {/each}
  </div>
</div>

<div class="footercontainer">
  <div class="w-full max-w-1440 mx-auto">
    {#each Maybe.toArray(resources) as { version }}
      <Footer {version} />
    {/each}
  </div>
</div>
