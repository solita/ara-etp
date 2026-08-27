<script>
  // The purpose of this component is to handle the display of toimenpide-ehdotukset (huomiot)
  // depending on whether the PPP is in use or not.
  import { _ } from '@Language/i18n';
  export let hasPpp = false;
</script>

{#if hasPpp}
  <p>
    <span class="mr-1 font-icon">info</span>
    {$_('energiatodistus.huomiot.ppp-kaytossa-info')}
  </p>
  <details>
    <summary class="mt-2"
      >{$_(
        'energiatodistus.huomiot.tarkastele-disabloituja-huomioita'
      )}</summary>
    <slot />
  </details>
{:else}
  <slot />
{/if}
