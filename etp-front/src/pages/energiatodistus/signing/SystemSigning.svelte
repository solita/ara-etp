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

  // Subset of signing statuses used in signing with system.
  const notStartedStatus = Signing.status.not_started;
  const inProgressStatus = Signing.status.already_started;
  const signedStatus = Signing.status.signed;

  const initialStatus = R.fromPairs([
    [ET.tila.draft, notStartedStatus],
    [ET.tila['in-signing'], inProgressStatus],
    [ET.tila.signed, signedStatus]
  ]);

  let stateIsSetViaStateInitialization = false;

  export let currentState;
  const setStatus = newStatus => {
    stateIsSetViaStateInitialization = false;
    currentState = R.assoc('status', newStatus, currentState);
  };
  const getStatus = state => R.prop('status', state);

  const statusText = Signing.statusText(i18n);

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
        const errorKey =
          'energiatodistus.signing.error.' + R.path(['body', 'type'], response);
        const message = i18n(errorKey);
        error = R.equals(message, errorKey)
          ? Maybe.Some(i18n('energiatodistus.signing.error.signing-failed'))
          : Maybe.Some(message);
        setStatus(notStartedStatus);
      },
      _ => {
        setStatus(signedStatus);
      },
      signAllPdfs(energiatodistus)
    );
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
        if (
          resp === `Energiatodistus ${energiatodistus.id} is already signed`
        ) {
          error = Maybe.Some(
            i18n('energiatodistus.signing.error.abort-failed')
          );
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
  stateIsSetViaStateInitialization = true;
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
    {#if !freshSession}
      <p data-cy="signing-info-relogin">
        {i18n('energiatodistus.signing.system-signing-info-text-relogin')}
      </p>
    {/if}
    <div class="buttons">
      <div class="mr-10 mt-5">
        {#if freshSession}
          <Button
            prefix="signing-submit"
            text={i18n('energiatodistus.signing.button.start')}
            on:click={sign} />
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
    <div class="mt-2">
      <Spinner />
    </div>
    <div class="buttons">
      <!-- Only show the possibility to abort if someone reloads the page. -->
      {#if stateIsSetViaStateInitialization}
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
      {/if}
    </div>
  {/if}
</div>
