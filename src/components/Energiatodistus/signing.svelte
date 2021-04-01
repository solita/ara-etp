<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Objects from '@Utility/objects';

  import * as Kielisyys from './kielisyys';
  import * as ET from './energiatodistus-utils';
  import * as etApi from './energiatodistus-api';
  import * as signatureApi from './signature-api';
  import { _ } from '@Language/i18n';

  import Button from '@Component/Button/Button';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';

  export let energiatodistus;
  export let reload;

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

  const statuses = [
    'not_started',
    'already_started',
    'start',
    'digest',
    'signature',
    'pdf',
    'finish',
    'signed',
    'aborted'
  ];

  const status = R.compose(R.map(parseInt), R.invertObj)(statuses);

  /* system tasks wait response from backend - users tasks wait input from user */
  const systemTasks = [status.start, status.digest, status.pdf, status.finish];
  const closeTasks = [status.not_started, status.signed, status.aborted];

  const statusKey = id => statuses[id];

  let currentState = {};
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
    [ET.tila.draft, status.not_started],
    [ET.tila['in-signing'], status.already_started],
    [ET.tila.signed, status.signed]
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

  const capitalize = R.compose(
    R.join(''),
    R.juxt([R.compose(R.toUpper, R.head), R.tail])
  );

  const statusText = state => {
    const languageAdjectiveName = $_(
      'energiatodistus.signing.language-adjective.' + state.language
    );
    const languageAdjectiveGenetiveName = $_(
      'energiatodistus.signing.language-genitive.' + state.language
    );

    return R.compose(
      capitalize,
      R.replace('{language}', languageAdjectiveName),
      R.replace('{language-genitive}', languageAdjectiveGenetiveName),
      key => $_('energiatodistus.signing.status.' + key),
      R.replace('_', '-'),
      statusKey
    )(state.status);
  };

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
      run(setState(status.pdf, language)),
      R.chain(signatureApi.getSignature(fetch)),
      run(setState(status.signature, language)),
      R.chain(etApi.digest(fetch, energiatodistus.versio, energiatodistus.id)),
      run(setState(status.digest, language)),
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
      const message = $_(errorKey);
      error = R.equals(message, errorKey)
        ? Maybe.Some($_('energiatodistus.signing.error.signing-failed'))
        : Maybe.Some(message);
    }, setStateStatus(status.signed)),
    Future.and(
      etApi.finishSign(fetch, energiatodistus.versio, energiatodistus.id)
    ),
    run(setStateStatus(status.finish)),
    Future.and(signAllPdf),
    _ => etApi.startSign(fetch, energiatodistus.versio, energiatodistus.id),
    setStateStatus(status.start)
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
        error = Maybe.Some($_('energiatodistus.signing.error.abort-failed'));
      },
      setStateStatus(status.aborted),
      etApi.cancelSign(fetch, energiatodistus.versio, energiatodistus.id)
    );
  };

  const pdfUrl = language =>
    etApi.url.pdf(energiatodistus.versio, energiatodistus.id, language);
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

  p {
    @apply mt-2;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }

  .error {
    @apply flex py-2 px-2 bg-error text-light;
  }
</style>

<dialog on:click|stopPropagation>
  <div class="content">
    <h1>{$_('energiatodistus.signing.header')}</h1>

    {#if mPollux.failure.isSome()}
      <p>
        {$_('energiatodistus.signing.messages.connection-failed')}
      </p>
    {:else if mPollux.version.isSome()}
      {#if R.equals(currentState.status, status.already_started)}
        <p>
          {$_('energiatodistus.signing.messages.already-started')}
        </p>
      {/if}

      {#if R.includes(currentState.status, [
        status.not_started,
        status.already_started
      ])}
        <p>
          {$_('energiatodistus.signing.messages.connection-success')}
        </p>
      {/if}

      {#each error.toArray() as txt}
        <div class="my-2 error">
          <span class="font-icon mr-2">error_outline</span>
          <div>{txt}</div>
        </div>
      {/each}

      <p>{statusText(currentState)}</p>
      {#if R.includes(currentState.status, systemTasks) && error.isNone()}
        <div class="mt-2"><Spinner /></div>
      {/if}

      {#if R.equals(currentState.status, status.signed)}
        <div class="flex flex-col items-start mt-2">
          {#if Kielisyys.fi(energiatodistus)}
            <Link
              href={pdfUrl('fi')}
              target={'_blank'}
              text={`energiatodistus-${energiatodistus.id}-fi.pdf`} />
          {/if}
          {#if Kielisyys.sv(energiatodistus)}
            <Link
              href={pdfUrl('sv')}
              target={'_blank'}
              text={`energiatodistus-${energiatodistus.id}-sv.pdf`} />
          {/if}
        </div>
      {/if}
    {:else}
      <p>
        {$_('energiatodistus.signing.messages.connection-check')}
      </p>
      <Spinner />
    {/if}

    {#each mPollux.version.toArray() as success}
      <p class="text-secondary">
        mPollux {success.version}
      </p>
    {/each}

    <div class="buttons">
      {#if (R.includes(currentState.status, [
        status.not_started,
        status.already_started
      ]) && mPollux.version.isSome()) || error.isSome()}
        <div class="mr-10 mt-5">
          <Button
            prefix="signing-submit"
            text={$_('energiatodistus.signing.button.start')}
            on:click={sign} />
        </div>
      {/if}
      {#if !R.includes(currentState.status, closeTasks)}
        <div class="mt-5">
          <Button
            prefix="signing-reject"
            text={$_('energiatodistus.signing.button.abort')}
            style={'secondary'}
            on:click={abort} />
        </div>
      {/if}
      {#if R.includes(currentState.status, closeTasks)}
        <div class="mt-5">
          <Button
            text={$_('energiatodistus.signing.button.close')}
            style={'secondary'}
            on:click={reload} />
        </div>
      {/if}
    </div>
  </div>
</dialog>
