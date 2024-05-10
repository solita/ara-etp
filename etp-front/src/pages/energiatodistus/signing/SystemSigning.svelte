<script>
  import Button from '@Component/Button/Button.svelte';
  import { _ } from '@Language/i18n';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as R from 'ramda';
  import * as etApi from '@Pages/energiatodistus/energiatodistus-api';
  import Error from '@Component/Error/Error.svelte';
  import * as Signing from './signing';
  import * as Kielisyys from '@Pages/energiatodistus/kielisyys.js';
  import Link from '@Component/Link/Link.svelte';

  export let energiatodistus;
  export let reload;

  const i18n = $_;

  let error = Maybe.None();
  let inProgress = false;

  let signingSucceeded = false;

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
        inProgress = false;
        signingSucceeded = false;
      },
      _ => {
        signingSucceeded = true;
        inProgress = false;
      },
      signAllPdfs(energiatodistus)
    );
  };

  const sign = () => {
    inProgress = true;
    error = Maybe.None();
    signingProcess();
  };
</script>

<style type="text/postcss">
  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  <div>{i18n('energiatodistus.signing.system-signing-info-text')}</div>

  {#each error.toArray() as text}
    <Error {text} />
  {/each}

  {#if signingSucceeded}
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
  {/if}

  {#if inProgress}
    <div class="mt-2">
      <Spinner />
    </div>
  {/if}

  <div class="buttons">
    {#if !signingSucceeded}
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-submit"
          disabled={inProgress}
          text={i18n('energiatodistus.signing.button.start')}
          on:click={sign} />
      </div>
    {/if}
    <div class="mt-5">
      <Button
        prefix="signing-close"
        disabled={inProgress}
        text={i18n('energiatodistus.signing.button.close')}
        style={'secondary'}
        on:click={reload} />
    </div>
  </div>
</div>
