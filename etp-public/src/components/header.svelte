<script>
  import { slide, scale } from 'svelte/transition';
  import Logo from '@Asset/logo.svg';
  import NavBar from '@Component/nav-bar';
  import Container, { styles as containerStyles } from '@Component/container';
  import { setLocale, locale, _ } from '@Localization/localization';

  export let config;

  let mobileNavShown = false;

  const closeMobileNav = () => {
    mobileNavShown = false;
  };
</script>

<style>
  .menu-icon {
    grid-column: 1;
    grid-row: 1;
    padding-top: 0.2em;
    font-size: 2em;
    @apply border-green text-green;
  }

  button:focus-within .menu-icon {
    animation: animate 1s linear infinite;
  }
  @keyframes animate {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(0.8);
    }
    100% {
      transform: scale(1);
    }
  }

  button:focus span {
    @apply text-ashblue;
  }
  button:focus .menu-icon-border {
    @apply border-ashblue font-bold text-ashblue;
  }

  @media print {
    header {
      display: none;
    }
  }
</style>

<header>
  <Container {...containerStyles.white}>
    <div class="flex justify-between items-center px-2 py-2 xl:px-16">
      <div class="flex justify-between items-center w-full">
        <a class="flex items-center" href="/">
          <img src={Logo} alt={$_('HEADER_ARA_LOGO_ALT')} class="h-12" />
          <h1 class="pl-2 text-xs xs:text-base tracking-widest">
            {$_('ENERGIATODISTUSREKISTERI')}
          </h1>
        </a>
        <nav
          class="font-semibold text-ashblue justify-start ml-4 mr-auto hidden lg:block">
          <button
            lang="fi"
            class="font-semibold"
            class:underline={$locale == 'sv'}
            class:text-darkgreen={$locale == 'sv'}
            on:click={() => setLocale('fi')}>
            suomeksi
          </button>
          |
          <button
            lang="sv"
            class="font-semibold"
            class:underline={$locale == 'fi'}
            class:text-darkgreen={$locale == 'fi'}
            on:click={() => setLocale('sv')}>
            på svenska
          </button>
        </nav>
        <a
          class="text-ashblue items-center hidden lg:flex"
          href="/rekisteroitymisohjeet"
          ><span class="font-bold hover:underline"
            >{$_('REKISTEROITYMISOHJEET')}</span>
          <span class="material-icons" aria-hidden="true"> chevron_right </span>
        </a>
        {#if config}
          <a
            class="text-green items-center hidden ml-4 lg:flex"
            href={config.privateSiteUrl}
            ><span class="font-bold hover:underline"
              >{$_('NAVBAR_KIRJAUTUMINEN')}</span>
            <span class="material-icons" aria-hidden="true">
              chevron_right
            </span></a>
        {/if}
      </div>
      <button
        class="flex items-center lg:hidden focus:outline-none focus:text-black rounded-md text-green"
        on:click={() => {
          mobileNavShown = !mobileNavShown;
        }}>
        <span class="menu-label uppercase font-bold hidden sm:block mr-1"
          >{$_('MAIN_MENU')}</span>
        <div
          class="menu-icon-border border-2 border-green rounded-full w-12 h-12 grid overflow-hidden icon-container">
          {#if mobileNavShown}
            <span
              class="material-icons menu-icon w-full h-full"
              aria-hidden="true"
              transition:scale>
              close
            </span>
          {:else}
            <span
              class="material-icons menu-icon w-full h-full"
              aria-hidden="true"
              transition:scale>
              menu
            </span>
          {/if}
        </div>
      </button>
    </div>
  </Container>
  {#if mobileNavShown}
    <div transition:slide class="lg:hidden">
      <NavBar navButtonClicked={closeMobileNav} />
      <a
        class="text-ashblue items-center justify-center flex py-4 bg-white"
        href="/rekisteroitymisohjeet"
        on:click={() => {
          closeMobileNav();
        }}>
        <span class="font-bold hover:underline"
          >{$_('REKISTEROITYMISOHJEET')}</span>
        <span class="material-icons ml-1" aria-hidden="true"> info </span></a>
      {#if config}
        <a
          class="text-green items-center justify-center flex py-4 bg-white"
          href={config.privateSiteUrl}>
          <span class="font-bold hover:underline"
            >{$_('NAVBAR_KIRJAUTUMINEN')}</span>
          <span class="material-icons" aria-hidden="true">
            chevron_right
          </span></a>
      {/if}

      <nav class="font-semibold text-ashblue p-3 text-center mx-auto bg-grey">
        <button
          lang="fi"
          class="p-2 font-semibold"
          class:underline={$locale == 'sv'}
          class:text-darkgreen={$locale == 'sv'}
          on:click={() => {
            closeMobileNav();
            setLocale('fi');
          }}>
          suomeksi
        </button>
        |
        <button
          lang="sv"
          class="p-2 font-semibold"
          class:underline={$locale == 'fi'}
          class:text-darkgreen={$locale == 'fi'}
          on:click={() => {
            closeMobileNav();
            setLocale('sv');
          }}>
          på svenska
        </button>
      </nav>
    </div>
  {/if}

  <div class="hidden lg:block">
    <NavBar {config} />
  </div>
</header>
