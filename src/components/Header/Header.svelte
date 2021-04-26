<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import * as Navigation from '@Utility/navigation';
  import * as Kayttajat from '@Utility/kayttajat';
  import { _ } from '@Language/i18n';

  import LanguageSelect from './language-select';

  export let whoami = Maybe.None();
  export let ohjeSivut = Maybe.None();

  let showNameDropdown = false;
  let showOhjeDropdown = false;
  let nameNode;
  let ohjeNode;

  const fullName = kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`;

  $: links = R.compose(
    R.concat(R.__, Navigation.defaultHeaderMenuLinks($_)),
    Maybe.orSome([]),
    R.map(Navigation.roleBasedHeaderMenuLinks($_))
  )(whoami);

  const hasNoParent = s => R.prop('parent-id', s).isNone();
  const sortByOrdinalTitle = R.sortWith([
    R.ascend(R.prop('ordinal')),
    R.ascend(R.compose(R.toLower, R.prop('title')))
  ]);
  let ohjeNav = R.compose(sortByOrdinalTitle, R.filter(hasNoParent))(ohjeSivut);
</script>

<style type="text/postcss">
  header {
    @apply flex px-10 h-20 uppercase text-light justify-between items-center font-bold tracking-xl;
  }

  .listlink {
    @apply px-4 py-2 text-dark text-center font-normal normal-case w-full tracking-normal cursor-pointer truncate;
  }

  .listlink:not(:last-child) {
    @apply border-b-1 border-disabled;
  }

  .listlink:hover {
    @apply bg-background;
  }

  .logout {
    @apply normal-case text-sm font-normal tracking-normal;
  }

  .logout:hover {
    @apply cursor-pointer underline;
  }
</style>

<svelte:window
  on:click={event => {
    if (!R.isNil(nameNode)) {
      if (
        event.target !== nameNode &&
        event.target.parentElement !== nameNode
      ) {
        showNameDropdown = false;
      }
    }
    if (!R.isNil(ohjeNode)) {
      if (
        event.target !== ohjeNode &&
        event.target.parentElement !== ohjeNode
      ) {
        showOhjeDropdown = false;
      }
    }
  }} />

<header class="flex justify-between">
  <div class="flex flex-1 items-center">
    <a href="/">
      <img class="mr-8" src="images/ara_logo_simple_nega.svg" alt="Ara" />
    </a>
    <LanguageSelect />
  </div>

  <div class="dropdowns flex space-x-8 items-center">
    {#if !R.isEmpty(ohjeNav)}
      <div class="flex flex-row justify-between">
        <div
          bind:this={ohjeNode}
          class="relative cursor-pointer"
          on:click={() => (showOhjeDropdown = !showOhjeDropdown)}>
          <span> {$_('navigation.ohjeet')} </span>
          <span class="material-icons absolute">keyboard_arrow_down</span>
          {#if showOhjeDropdown}
            <div
              class="absolute mt-2 w-48 bg-light shadow-xl flex flex-col z-10">
              {#each ohjeNav as sivu}
                <a class="listlink w-full" href={`/#/ohje/${sivu.id}`}>
                  {sivu.title}
                </a>
              {/each}
            </div>
          {/if}
        </div>
      </div>
    {:else if Kayttajat.isPaakayttaja(R.head(whoami.toArray()))}
      <a class="text-light flex items-center" href={'/#/ohje/new'}>
        <span class="material-icons"> add </span>
        <span>{$_('navigation.ohjeet')}</span>
      </a>
    {/if}

    {#each whoami.toArray() as user}
      <div class="flex flex-row justify-between">
        <div
          bind:this={nameNode}
          class="relative cursor-pointer"
          on:click={() => (showNameDropdown = !showNameDropdown)}>
          <span>
            {fullName(user)}
          </span>
          <span class="material-icons absolute">keyboard_arrow_down</span>
          {#if showNameDropdown}
            <div
              class="absolute mt-2 w-48 bg-light shadow-xl flex flex-col z-10">
              {#each links as link}
                <a class="listlink w-full" href={link.href}>{link.text}</a>
              {/each}
            </div>
          {/if}
        </div>
      </div>
    {/each}
  </div>

  {#if whoami.isNone()}
    <div class="logout px-2">
      <a href="api/logout">{$_('navigation.kirjaudu-ulos')}</a>
    </div>
  {/if}
</header>
