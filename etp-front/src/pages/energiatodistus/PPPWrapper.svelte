<script>
  import { _ } from '@Language/i18n';
  import H2 from '@Component/H/H2';
  import HR from '@Component/HR/HR';
  import TextButton from '@Component/Button/TextButton';

  export let showPPP;
  export let onAddPPP;
</script>

<style>
  .ppp-section :global(h2) {
    margin-bottom: 0;
  }
</style>

<HR />
<div class="ppp-section flex flex-col gap-6">
  <div class="flex items-baseline justify-between">
    <H2
      id="perusparannuspassi"
      text={$_('energiatodistus.perusparannuspassi.header')} />
    {#if !showPPP}
      <TextButton
        icon="add_circle_outline"
        text={$_('energiatodistus.perusparannuspassi.add-button')}
        type="button"
        on:click={onAddPPP} />
    {/if}
  </div>
  <div class="flex items-start items-center bg-tertiary p-4">
    <span class="mr-2 font-icon text-2xl">info_outline</span>
    <span>{$_('energiatodistus.perusparannuspassi.info-text')}</span>
  </div>

  {#if !showPPP}
    <p>
      {$_('energiatodistus.perusparannuspassi.not-added')}
    </p>
    <p>
      {$_('energiatodistus.perusparannuspassi.disclaimer')}
    </p>
  {:else}
    <p>
      {$_('energiatodistus.perusparannuspassi.disclaimer')}
    </p>
    <HR />
    <slot />
  {/if}
</div>
<HR />
