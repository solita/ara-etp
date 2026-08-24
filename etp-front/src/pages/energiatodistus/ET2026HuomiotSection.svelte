<script>
  import {_ } from '@Language/i18n';
  export let hasPpp = false;
</script>

{#if hasPpp}
  <p class="ml-1 mt-2 flex text-sm">
    <span class="mr-1 font-icon">info</span>
    {$_('energiatodistus.huomiot.ppp-kaytossa-info')}
  </p>
  <div class="bg-grey m-8">
  <details>
      <slot />
  </details>
  </div>
{:else}
  <slot />
{/if}
