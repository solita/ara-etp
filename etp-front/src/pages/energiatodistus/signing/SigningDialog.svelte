<script>
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import SystemSigning from './SystemSigning.svelte';
  import * as Signing from './signing';
  import * as Future from '@Utility/future-utils';
  import { announcementsForModule } from '@Utility/announce.js';

  export let energiatodistus;
  export let reload;

  const i18n = $_;

  export let freshSession = false;

  let currentState = { status: Signing.status.not_started };

  const canShowInstructions = state =>
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

    {#if canShowInstructions(currentState)}
      <div class="mt-2" data-cy="signing-instructions">
        <p>{i18n('energiatodistus.signing.instructions')}</p>
      </div>
    {/if}
    <div class="mt-4">
      <SystemSigning
        {energiatodistus}
        {reload}
        bind:currentState
        bind:freshSession />
    </div>
  </div>
</dialog>
