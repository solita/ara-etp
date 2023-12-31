<script>
  import { location } from 'svelte-spa-router';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Response from '@Utility/response';

  import { setupI18n, _ } from '@Language/i18n';
  import Header from '@Pages/header/Header';
  import Loading from '@Component/Loading/Loading';
  import Login from '@Component/Login/Login';
  import Error from './Error.svelte';
  import Footer from '@Pages/footer/Footer';
  import Uloskirjauduttu from '@Pages/uloskirjauduttu/uloskirjauduttu';

  import TableStyles from '@/TableStyles';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as versionApi from '@Component/Version/version-api';
  import * as ohjeApi from '@Pages/ohje/ohje-api';
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
    },
    Future.parallelObject(4, {
      whoami: kayttajaApi.whoami,
      version: versionApi.getVersion,
      config: versionApi.getConfig,
      ohjeSivut: ohjeApi.getSivut
    })
  );
</script>

<style type="text/postcss">
  .appcontainer {
    @apply w-full flex flex-col flex-grow justify-between min-h-screen bg-light;
  }

  .headercontainer,
  .footercontainer {
    @apply w-full flex justify-center;
  }

  .headercontainer {
    @apply bg-secondary;
  }

  .footercontainer {
    @apply bg-ara-2021-light-gray-background;
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
    {#if $location === '/uloskirjauduttu'}
      <Uloskirjauduttu />
    {:else if Response.isUnauthorized(error)}
      <Login redirectTimeout={2000} />
    {:else}
      <Error {error} />
    {/if}
  {/each}

  {#each Maybe.toArray(resources) as { whoami, config, version, ohjeSivut }}
    <div class="headercontainer">
      <div class="w-full max-w-1440 mx-auto">
        <Header whoami={Maybe.Some(whoami)} {ohjeSivut} />
      </div>
    </div>

    <div class="flex flex-col flex-grow">
      <Body {whoami} {config} />
    </div>

    <div class="footercontainer">
      <div class="w-full max-w-1440 mx-auto">
        <Footer {version} />
      </div>
    </div>
  {/each}
</div>
