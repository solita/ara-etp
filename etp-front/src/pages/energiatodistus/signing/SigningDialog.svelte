<script>
  import { _ } from '@Language/i18n';

  import SystemSigning from './SystemSigning.svelte';
  import * as Signing from './signing';
  import * as Future from '@Utility/future-utils';
  import { announcementsForModule } from '@Utility/announce.js';

  export let energiatodistus;
  export let reload;

  const i18n = $_;

  export let freshSession = false;

  let currentState = Signing.dialogState.not_started;

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
    @apply fixed left-0 top-0 z-50 flex h-screen w-screen cursor-default items-center justify-center bg-hr;
  }

  .content {
    @apply relative flex w-2/3 flex-col justify-center rounded-md bg-light px-10 py-10 shadow-lg;
  }

  h1 {
    @apply mb-4 border-b-1 border-tertiary pb-2 text-lg font-bold uppercase tracking-xl text-secondary;
  }
</style>

<dialog on:click|stopPropagation open>
  <div class="content">
    <h1>{i18n('energiatodistus.signing.header')}</h1>

    <div class="mt-4">
      <SystemSigning
        {energiatodistus}
        {reload}
        bind:currentState
        bind:freshSession />
    </div>
  </div>
</dialog>
