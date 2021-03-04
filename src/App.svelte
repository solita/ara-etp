<script>
  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';
  import * as R from 'ramda';

  import { currentUserStore } from '@/stores';
  import { setupI18n, _ } from '@Language/i18n';
  import Header from '@Component/Header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Error from './Error.svelte';
  import Footer from '@Component/Footer/Footer';

  import TableStyles from '@/TableStyles';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as versionApi from '@Component/Version/version-api';
  import * as Future from '@Utility/future-utils';
  import Body from './Body.svelte';

  setupI18n();

  let resources = Maybe.None();
  let failure = Maybe.None();

  Future.fork(
    response => {
      failure = Maybe.Some(response);
    },
    response => {
      resources = Maybe.Some(response);
      currentUserStore.set(Maybe.Some(response.whoami));
    },
    Future.parallelObject(3, {
      whoami: kayttajaApi.whoami,
      version: versionApi.getVersion,
      config: versionApi.getConfig
    })
  );
</script>

<style type="text/postcss">
  .appcontainer {
    @apply flex flex-col flex-grow justify-between min-h-screen;
  }

  .headercontainer,
  .footercontainer {
    @apply flex justify-center;
  }

  .headercontainer {
    @apply bg-secondary;
  }

  .footercontainer {
    @apply bg-background;
  }
</style>

<TableStyles />

<div class="appcontainer font-body">
  {#if resources.isNone() && failure.isNone()}
    <section class="flex flex-col flex-grow py-8 px-10 mx-auto">
      <Loading />
    </section>
  {/if}

  {#each Maybe.toArray(failure) as error}
    {#if Response.isUnauthorized(error)}
      <Login redirectTimeout={2000} />
    {:else}
      <Error {error} />
    {/if}
  {/each}

  {#each Maybe.toArray(resources) as { whoami, config, version }}
    <div class="headercontainer">
      <div class="xl:w-xl lg:w-lg md:w-md sm:w-sm">
        <Header whoami={Maybe.Some(whoami)} />
      </div>
    </div>

    <Body {whoami} {config} />

    <div class="footercontainer">
      <div class="xl:w-xl lg:w-lg md:w-md sm:w-sm">
        <Footer {version} />
      </div>
    </div>
  {/each}
</div>
