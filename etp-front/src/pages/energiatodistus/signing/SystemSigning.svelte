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

  const possibleStates = [
    'ready_to_sign',
    'in_progress',
    'signing_succeeded',
    'aborted'
  ];

  //TODO: Intial state depends on the state in the backend.
  let state = 'ready_to_sign';

  const setState = newState => state = newState;

  const statusText = Signing.statusText(i18n);

  const signAllPdfs = energiatodistus => {
    return etApi.signPdfsUsingSystemSignature(
      fetch,
      energiatodistus.versio,
      energiatodistus.id
    );
  };

  const signingProcess = () => {
    return Future.fork(
      response => {
        const errorKey =
          'energiatodistus.signing.error.' + R.path(['body', 'type'], response);
        const message = i18n(errorKey);
        error = R.equals(message, errorKey)
          ? Maybe.Some(i18n('energiatodistus.signing.error.signing-failed'))
          : Maybe.Some(message);
        setState('ready_to_sign');
      },
      _ => {
        setState('signing_succeeded');
      },
      signAllPdfs(energiatodistus)
    );
  };

  let cancel = _ => {
  };

  const sign = () => {
    setState('in_progress');
    error = Maybe.None();
    cancel = signingProcess();
  };

  const abort = () => {
    cancel();
    Future.fork(
      _ => {
        error = Maybe.Some(i18n('energiatodistus.signing.error.abort-failed'));
      },
      () => {
        setState('aborted');
      },
      etApi.cancelSign(fetch, energiatodistus.versio, energiatodistus.id)
    );
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

  {#if state === 'aborted'}
    <p>
      {statusText({
        status: Signing.status.aborted,
        language: Kielisyys.getEnergiatodistusLanguageCode(energiatodistus)
      })}
    </p>
    <div class="buttons">
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-submit"
          text={i18n('energiatodistus.signing.button.start')}
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

  {:else if state === 'ready_to_sign'}
    <div class="buttons">
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-submit"
          text={i18n('energiatodistus.signing.button.start')}
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

  {:else if state === 'signing_succeeded'}
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
  {:else if state === 'in_progress'}
    <div class="mt-2">
      <Spinner />
    </div>
    <div class="buttons">
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-reject"
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
