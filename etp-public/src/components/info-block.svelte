<script>
  import IconInfo from '@Asset/icons/info_filled.svg';
  import { _ } from '@Localization/localization';

  export let title;
  export let showIcon = true;
</script>

<style>
  section {
    min-height: 12rem;
  }

  .icon {
    width: 4em;
    height: 4em;
  }

  .icon.smaller {
    width: 2.5em;
    height: 2.5em;
  }
</style>

<section
  class="flex flex-col justify-center items-left bg-beige sm:px-16 sm:py-8 px-4 py-4">
  {#if showIcon}
    <img
      class="icon smaller sm:hidden mb-2"
      src={IconInfo}
      alt={$_('INFO_ICON_ALT')} />
  {/if}
  <h2 class="font-bold text-ashblue text-xl tracking-widest mb-4 text-left">
    {title}
  </h2>
  <div class="flex items-center">
    {#if showIcon}
      <img
        class="icon mr-4 hidden sm:block"
        src={IconInfo}
        alt={$_('INFO_ICON_ALT')} />
    {/if}
    <p class="font-medium whitespace-pre-line">
      <slot />
    </p>
  </div>
</section>
