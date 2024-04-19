<script>
  import { _ } from '@Language/i18n';

  import CardSigning from './CardSigning.svelte';
  import Radio from '@Component/Radio/Radio.svelte';
  import SystemSigning from './SystemSigning.svelte';
  import SigningInstructions from './SigningInstructions.svelte';
  import * as Future from '@Utility/future-utils';
  import * as versionApi from '@Component/Version/version-api';
  import { isProduction } from '@Utility/config_utils';

  export let energiatodistus;
  export let reload;

  export let selection = 'card';

  const i18n = $_;

  const isSigningMethodCard = selectedMethod => selectedMethod === 'card';

  export let allowSelection = false;
  export let checkIfSelectionIsAllowed = false;

  if (checkIfSelectionIsAllowed) {
    Future.fork(
      _ => {
        allowSelection = false;
      },
      config => {
        if (!isProduction(config.environment)) {
          allowSelection = true;
        }
      },
      versionApi.getConfig
    );
  }
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .selection {
    @apply flex mt-4;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }
</style>

<dialog on:click|stopPropagation>
  <div class="content">
    <h1>{i18n('energiatodistus.signing.header')}</h1>

    <div class="mt-2">
      <SigningInstructions />
    </div>

    {#if allowSelection}
      <div class="mt-2">
        <div class="selection">
          <div class="mr-3">
            <Radio
              label="Käytä korttia"
              bind:group={selection}
              value="card"
              name="Card" />
          </div>
          <div class="mr-3">
            <Radio
              label="Älä käytä korttia"
              bind:group={selection}
              value="system"
              name="System" />
          </div>
        </div>
      </div>
    {/if}

    <div class="mt-4">
      {#if isSigningMethodCard(selection)}
        <CardSigning {energiatodistus} {reload} />
      {:else}
        <SystemSigning {energiatodistus} {reload} />
      {/if}
    </div>
  </div>
</dialog>
