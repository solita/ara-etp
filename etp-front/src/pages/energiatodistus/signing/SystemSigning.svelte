<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Objects from '@Utility/objects';
  import * as R from 'ramda';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as etApi from '@Pages/energiatodistus/energiatodistus-api';
  import Error from '@Component/Error/Error.svelte';
  import * as Signing from './signing';
  import * as Kielisyys from '@Pages/energiatodistus/kielisyys.js';
  import Link from '@Component/Link/Link.svelte';
  import { redirect } from '@Utility/redirect-utils.js';

  export let energiatodistus;
  export let reload;
  export let freshSession;

  const i18n = $_;

  let error = Maybe.None();

  // Signing statuses used in signing with system.
  const notStartedStatus = Signing.status.not_started;
  const askForConfirmationStatus = Signing.status.confirming_start;
  const inProgressStatus = Signing.status.in_progress;
  const inProgressReloadedStatus = Signing.status.in_progress_reloaded;
  const signedStatus = Signing.status.signed;

  const initialStatus = R.fromPairs([
    [ET.tila.draft, notStartedStatus],
    [ET.tila['in-signing'], inProgressReloadedStatus],
    [ET.tila.signed, signedStatus]
  ]);

  export let currentState;
  const setStatus = newStatus => {
    currentState = R.assoc('status', newStatus, currentState);
  };
  const getStatus = state => R.prop('status', state);

  const statusText = Signing.statusText(i18n);

  const isAlreadySignedResponse =
    R.equals(R.__, `Energiatodistus ${energiatodistus.id} is already signed`);

  const signAllPdfs = energiatodistus => {
    return etApi.signPdfsUsingSystemSignature(
      fetch,
      energiatodistus.versio,
      energiatodistus.id
    );
  };

  const signingProcess = () => {
    Future.fork(
      response => {
        if (isAlreadySignedResponse(response.body)) {
          error = Maybe.Some(i18n('energiatodistus.signing.error.already-signed'));
        } else {
          const errorKey =
            'energiatodistus.signing.error.' + R.path(['body', 'type'], response);
          const message = i18n(errorKey);
          error = R.equals(message, errorKey)
            ? Maybe.Some(i18n('energiatodistus.signing.error.signing-failed'))
            : Maybe.Some(message);
        }
        setStatus(notStartedStatus);
      },
      _ => {
        setStatus(signedStatus);
      },
      signAllPdfs(energiatodistus)
    );
  };

  const showSigningConfirmation = () => {
    setStatus(askForConfirmationStatus);
  };

  const sign = () => {
    setStatus(inProgressStatus);
    error = Maybe.None();
    signingProcess();
  };

  const abort = _ => {
    Future.fork(
      _ => {
        error = Maybe.Some(i18n('energiatodistus.signing.error.abort-failed'));
      },
      resp => {
        if (isAlreadySignedResponse(resp)) {
          error = Maybe.Some(i18n('energiatodistus.signing.error.already-signed'));
        } else reload();
      },
      etApi.cancelSign(fetch, energiatodistus.versio, energiatodistus.id)
    );
  };

  const relogin = () => {
    redirect(`/api/logout?redirect-location=/${location.hash}`);
  };

  setStatus(
    Objects.requireNotNil(
      initialStatus[energiatodistus['tila-id']],
      'Energiatodistus ' +
      energiatodistus.id +
      ' invalid tila: ' +
      ET.tilaKey(energiatodistus['tila-id'])
    )
  );
</script>

<style type="text/postcss">
    .buttons {
        @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
    }
</style>

<div>
  {#each error.toArray() as text}
    <Error {text} />
  {/each}

  {#if getStatus(currentState) === notStartedStatus}
    <div class="mt-2" data-cy="signing-instructions">
      <p>{i18n('energiatodistus.signing.instructions')}</p>
    </div>
    {#if !freshSession}
      <p data-cy="signing-info-relogin">
        {i18n('energiatodistus.signing.system-signing-info-text-relogin')}
      </p>
    {/if}
    <div class="buttons">
      <div class="mr-10 mt-5">
        {#if freshSession}
          <Button
            prefix="signing-pre-submit"
            text={i18n('energiatodistus.signing.button.start')}
            on:click={showSigningConfirmation} />
        {:else}
          <Button
            prefix="relogin"
            text={i18n('energiatodistus.signing.button.relogin')}
            style={'secondary'}
            on:click={relogin} />
        {/if}
      </div>
      <div class="mt-5">
        <Button
          prefix="signing-close"
          text={i18n('energiatodistus.signing.button.close')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>

  {:else if getStatus(currentState) === askForConfirmationStatus}
    <div class="mt-2" data-cy="signing-instructions">
      <p>{i18n('energiatodistus.signing.system-signing-confirm-start-text')}</p>
    </div>
    <div class="buttons">
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-pre-submit"
          text={i18n('energiatodistus.signing.button.confirm-start')}
          on:click={sign} />
      </div>
      <div class="mt-5">
        <Button
          prefix="signing-close"
          text={i18n('energiatodistus.signing.button.close')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>

  {:else if getStatus(currentState) === signedStatus}
    <p>
      {statusText({
        status: Signing.status.signed,
        language: Kielisyys.getEnergiatodistusLanguageCode(energiatodistus)
      })}
    </p>

    <div class="flex flex-col items-start mt-2">
      {#if Kielisyys.fi(energiatodistus)}
        <Link
          href={Signing.pdfUrl(energiatodistus, 'fi')}
          target={'_blank'}
          text={`energiatodistus-${energiatodistus.id}-fi.pdf`} />
      {/if}
      {#if Kielisyys.sv(energiatodistus)}
        <Link
          href={Signing.pdfUrl(energiatodistus, 'sv')}
          target={'_blank'}
          text={`energiatodistus-${energiatodistus.id}-sv.pdf`} />
      {/if}
    </div>
    <div class="buttons">
      <div class="mt-5">
        <Button
          prefix="signing-close"
          text={i18n('energiatodistus.signing.button.close')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>

  {:else if getStatus(currentState) === inProgressStatus}
    <p data-cy="signing-status">
      {i18n('energiatodistus.signing.system-signing-status-text')}
    </p>
    <p>
      {i18n('energiatodistus.signing.system-signing-expected-duration-text')}
    </p>
    <div class="mt-2">
      <Spinner />
    </div>

  {:else if getStatus(currentState) === inProgressReloadedStatus}
    <p data-cy="signing-status">
      {i18n('energiatodistus.signing.system-signing-reloaded-status-text')}
    </p>
    <p>
      {i18n('energiatodistus.signing.system-signing-reloaded-abort-text')}
    </p>
    <div class="buttons">
      <!-- Only show the possibility to abort if someone reloads the page. -->
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-abort"
          text={i18n('energiatodistus.signing.button.abort')}
          style={'secondary'}
          on:click={abort} />
      </div>
      <div class="mt-5">
        <Button
          prefix="signing-close"
          text={i18n('energiatodistus.signing.button.close')}
          style={'secondary'}
          on:click={reload} />
      </div>
    </div>
  {/if}
</div>
