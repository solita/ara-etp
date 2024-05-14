<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Objects from '@Utility/objects';

  import * as Kielisyys from '../kielisyys';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as etApi from '@Pages/energiatodistus/energiatodistus-api';
  import * as signatureApi from './signature-api';
  import { _ } from '@Language/i18n';

  import Button from '@Component/Button/Button.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Error from '@Component/Error/Error.svelte';
  import * as Signing from './signing';

  export let energiatodistus;
  export let reload;

  const i18n = $_;

  let error = Maybe.None();
  let mPollux = {
    version: Maybe.None(),
    failure: Maybe.None()
  };

  Future.fork(
    response => {
      mPollux = R.assoc('failure', Maybe.Some(response), mPollux);
    },
    response => {
      mPollux = R.assoc('version', Maybe.Some(response), mPollux);
    },
    signatureApi.versionInfo(fetch)
  );

  /* system tasks wait response from backend - users tasks wait input from user */
  const systemTasks = [
    Signing.status.start,
    Signing.status.digest,
    Signing.status.pdf,
    Signing.status.finish
  ];
  const closeTasks = [
    Signing.status.not_started,
    Signing.status.signed,
    Signing.status.aborted
  ];

  export let currentState = {};

  const setState = (status, language) => _ => {
    currentState = {
      status: status,
      language: language
    };
  };
  const setStateStatus = status =>
    setState(
      status,
      Maybe.fold('fi', Kielisyys.kieliKey, energiatodistus.perustiedot.kieli)
    );

  const initialStatus = R.fromPairs([
    [ET.tila.draft, Signing.status.not_started],
    [ET.tila['in-signing'], Signing.status.already_started],
    [ET.tila.signed, Signing.status.signed]
  ]);

  // set initial status based on et tila
  setStateStatus(
    Objects.requireNotNil(
      initialStatus[energiatodistus['tila-id']],
      'Energiatodistus ' +
        energiatodistus.id +
        ' invalid tila: ' +
        ET.tilaKey(energiatodistus['tila-id'])
    )
  )();

  const statusText = Signing.statusText(i18n);

  const run = R.compose(R.map, R.tap);

  const signPdf = language =>
    R.compose(
      R.chain(
        etApi.signPdf(
          fetch,
          energiatodistus.versio,
          energiatodistus.id,
          language
        )
      ),
      run(setState(Signing.status.pdf, language)),
      R.chain(signatureApi.getSignature(fetch)),
      run(setState(Signing.status.signature, language)),
      R.chain(etApi.digest(fetch, energiatodistus.versio, energiatodistus.id)),
      run(setState(Signing.status.digest, language)),
      Future.resolve
    )(language);

  const signAllPdf = Kielisyys.bilingual(energiatodistus)
    ? Future.and(signPdf('sv'), signPdf('fi'))
    : Kielisyys.onlySv(energiatodistus)
      ? signPdf('sv')
      : signPdf('fi');

  const signingProcess = R.compose(
    Future.fork(response => {
      const errorKey =
        'energiatodistus.signing.error.' + R.path(['body', 'type'], response);
      const message = i18n(errorKey);
      error = R.equals(message, errorKey)
        ? Maybe.Some(i18n('energiatodistus.signing.error.signing-failed'))
        : Maybe.Some(message);
    }, setStateStatus(Signing.status.signed)),
    Future.and(
      etApi.finishSign(fetch, energiatodistus.versio, energiatodistus.id)
    ),
    run(setStateStatus(Signing.status.finish)),
    Future.and(signAllPdf),
    _ => etApi.startSign(fetch, energiatodistus.versio, energiatodistus.id),
    setStateStatus(Signing.status.start)
  );

  let cancel = _ => {};

  const sign = _ => {
    error = Maybe.None();
    cancel = signingProcess();
  };

  const abort = _ => {
    cancel();
    Future.fork(
      _ => {
        error = Maybe.Some(i18n('energiatodistus.signing.error.abort-failed'));
      },
      setStateStatus(Signing.status.aborted),
      etApi.cancelSign(fetch, energiatodistus.versio, energiatodistus.id)
    );
  };
</script>

<style type="text/postcss">
  p {
    @apply mt-2;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<div>
  {#if mPollux.failure.isSome()}
    <p>
      {i18n('energiatodistus.signing.messages.connection-failed')}
    </p>
  {:else if mPollux.version.isSome()}
    {#if R.equals(currentState.status, Signing.status.already_started)}
      <p>
        {i18n('energiatodistus.signing.messages.already-started')}
      </p>
    {/if}

    {#if R.includes( currentState.status, [Signing.status.not_started, Signing.status.already_started] )}
      <p>
        {i18n('energiatodistus.signing.messages.connection-success')}
      </p>
    {/if}

    {#each error.toArray() as text}
      <Error {text} />
    {/each}

    <p>{statusText(currentState)}</p>
    {#if R.includes(currentState.status, systemTasks) && error.isNone()}
      <div class="mt-2">
        <Spinner />
      </div>
    {/if}

    {#if R.equals(currentState.status, Signing.status.signed)}
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
  {:else}
    <p>
      {i18n('energiatodistus.signing.messages.connection-check')}
    </p>
    <Spinner />
  {/if}

  {#each mPollux.version.toArray() as success}
    <p class="text-secondary">
      mPollux {success.version}
    </p>
  {/each}

  <div class="buttons">
    {#if (R.includes( currentState.status, [Signing.status.not_started, Signing.status.already_started] ) && mPollux.version.isSome()) || error.isSome()}
      <div class="mr-10 mt-5">
        <Button
          prefix="signing-submit"
          text={i18n('energiatodistus.signing.button.start')}
          on:click={sign} />
      </div>
    {/if}
    {#if !R.includes(currentState.status, closeTasks)}
      <div class="mt-5">
        <Button
          prefix="signing-reject"
          text={i18n('energiatodistus.signing.button.abort')}
          style={'secondary'}
          on:click={abort} />
      </div>
    {/if}
    {#if R.includes(currentState.status, closeTasks)}
      <div class="mt-5">
        <Button
          prefix="signing-close"
          text={i18n('energiatodistus.signing.button.close')}
          style={'secondary'}
          on:click={reload} />
      </div>
    {/if}
  </div>
</div>
