<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';

  import * as Navigation from '@Utility/navigation';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as keys from '@Utility/keys';
  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import LanguageSelect from './language-select';

  export let whoami = Maybe.None();
  export let ohjeSivut = Maybe.None();

  $: headerLogoPath = Locales.isSV($locale)
    ? 'images/YM_Varke_vaaka_valk_SV_RGB.png'
    : 'images/YM_Varke_vaaka_valk_FI_RGB.png';

  let showNameDropdown = false;
  let showOhjeDropdown = false;
  let nameNode;
  let ohjeNode;

  const toggleNameDropdown = () => (showNameDropdown = !showNameDropdown);
  const toggleOhjeDropdown = () => (showOhjeDropdown = !showOhjeDropdown);

  const enterHandler = handler => e => {
    if (e.keyCode === keys.ENTER) {
      handler();
    }
  };

  const fullName = kayttaja => `${kayttaja.etunimi} ${kayttaja.sukunimi}`;

  $: links = R.compose(
    R.concat(R.__, Navigation.defaultHeaderMenuLinks($_)),
    Maybe.orSome([]),
    R.map(Navigation.roleBasedHeaderMenuLinks($_))
  )(whoami);

  const sortByOrdinalTitle = R.sortWith([
    R.ascend(R.prop('ordinal')),
    R.ascend(R.compose(R.toLower, R.prop('title')))
  ]);
  let ohjeNav = R.compose(
    sortByOrdinalTitle,
    R.filter(R.propSatisfies(Maybe.isNone, 'parent-id'))
  )(ohjeSivut);
</script>

<style type="text/postcss">
  header {
    @apply flex h-20 items-center justify-between font-bold uppercase tracking-xl text-light;
  }

  .logo-link {
    flex-shrink: 0;
  }

  .listlink {
    @apply w-full cursor-pointer px-4 py-2 text-center font-normal normal-case tracking-normal text-dark;
  }
  .ohjelistlink {
    @apply w-full cursor-pointer p-2 text-left font-normal normal-case tracking-normal text-dark;
  }

  .listlink:not(:last-child),
  .ohjelistlink:not(:last-child) {
    @apply border-b-1 border-disabled;
  }

  .listlink:hover,
  .ohjelistlink:hover {
    @apply bg-background;
  }

  .logout {
    @apply text-sm font-normal normal-case tracking-normal;
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

<header class="my-3 flex justify-between px-2 lg:px-10">
  <div class="flex grow items-center">
    <a class="logo-link" href="/">
      <img class="mr-2 h-20 lg:mr-6" src={headerLogoPath} alt="Varke" />
    </a>
    <LanguageSelect />
  </div>

  <div class="dropdowns flex items-center space-x-16">
    {#if !R.isEmpty(ohjeNav)}
      <div class="flex flex-row justify-between">
        <div
          bind:this={ohjeNode}
          class="relative cursor-pointer hover:underline"
          on:click={toggleOhjeDropdown}>
          <span tabindex="0" on:keydown={enterHandler(toggleOhjeDropdown)}>
            {$_('navigation.ohjeet')}
          </span>
          <span class="material-icons absolute">keyboard_arrow_down</span>
          {#if showOhjeDropdown}
            <div
              class="absolute z-10 mt-2 flex w-96 flex-col bg-light shadow-xl">
              {#each ohjeNav as sivu}
                <a class="ohjelistlink" href={`/#/ohje/${sivu.id}`}>
                  <span> {sivu.title} </span>
                  {#if !sivu.published}
                    <span class="font-icon text-dark"> visibility_off </span>
                  {/if}
                </a>
              {/each}
            </div>
          {/if}
        </div>
      </div>
    {:else if Kayttajat.isPaakayttaja(R.head(whoami.toArray()))}
      <a class="flex items-center text-light" href={'/#/ohje/new'}>
        <span class="material-icons"> add </span>
        <span class="hover:underline">{$_('navigation.ohjeet')}</span>
      </a>
    {/if}

    {#each whoami.toArray() as user}
      <div class="flex flex-row justify-between">
        <div
          bind:this={nameNode}
          class="relative mr-6 cursor-pointer hover:underline"
          on:click={toggleNameDropdown}>
          <span
            data-cy="fullname-in-header"
            tabindex="0"
            on:keydown={enterHandler(toggleNameDropdown)}>
            {fullName(user)}
          </span>
          <span class="material-icons absolute">keyboard_arrow_down</span>
          {#if showNameDropdown}
            <div
              class="absolute z-10 mt-2 flex w-48 flex-col bg-light shadow-xl">
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
