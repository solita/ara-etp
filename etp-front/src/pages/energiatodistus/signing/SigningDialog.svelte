<script>
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import CardSigning from './CardSigning.svelte';
  import Radio from '@Component/Radio/Radio.svelte';
  import SystemSigning from './SystemSigning.svelte';
  import SigningInstructions from './SigningInstructions.svelte';
  import * as Signing from './signing';
  import * as Future from '@Utility/future-utils';
  import * as versionApi from '@Component/Version/version-api';
  import { isProduction } from '@Utility/config_utils';
  import { announcementsForModule } from '@Utility/announce.js';

  export let energiatodistus;
  export let reload;
  export let whoami;

  export let selection = 'card';

  const i18n = $_;

  const isSigningMethodCard = selectedMethod => selectedMethod === 'card';

  export let allowSelection = true;
  export let checkIfSelectionIsAllowed = false;
  export let freshSession = false;

  let currentState = { status: Signing.status.not_started };

  if (checkIfSelectionIsAllowed) {
    Future.fork(
      _ => {
        allowSelection = false;
      },
      config => {
        // Allow signing temporarily for laatija id 79 to check that the signing
        // works as intended.
        allowSelection = !isProduction(config.environment) || whoami.id === 79;
      },
      versionApi.getConfig
    );
  }

  const isSigningMethodSelectionAllowed = state =>
    R.includes(R.prop('status', state), [
      Signing.status.not_started,
      Signing.status.aborted
    ]);

  Future.fork(
    _ => {
      announcementsForModule('Energiatodistus').announceError(
        i18n('energiatodistus.signing.error.session-validity-check-failed')
      );
      freshSession = false;
    },
    res => {
      freshSession = res;
    },
    Signing.signingAllowed(fetch)
  );
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

<dialog on:click|stopPropagation open>
  <div class="content">
    <h1>{i18n('energiatodistus.signing.header')}</h1>

    {#if allowSelection && isSigningMethodSelectionAllowed(currentState)}
      <div class="mt-2" data-cy="signing-instructions">
        <p>{i18n('energiatodistus.signing.instructions')}</p>
      </div>

      <div class="mt-2">
        <div class="selection">
          <div class="mr-3">
            <Radio
              label={i18n('energiatodistus.signing.options.card')}
              bind:group={selection}
              value="card"
              name="Card" />
          </div>
          <div class="mr-3">
            <Radio
              label={i18n('energiatodistus.signing.options.system')}
              bind:group={selection}
              value="system"
              name="System" />
          </div>
        </div>
      </div>
    {/if}

    <div class="mt-4">
      {#if isSigningMethodCard(selection)}
        <CardSigning {energiatodistus} {reload} bind:currentState />
      {:else}
        <SystemSigning
          {energiatodistus}
          {reload}
          bind:currentState
          bind:freshSession />
      {/if}
    </div>
  </div>
</dialog>
