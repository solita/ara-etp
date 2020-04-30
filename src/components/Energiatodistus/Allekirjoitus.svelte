<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';
  import * as etApi from './energiatodistus-api';
  import * as sgApi from './signature-api';

  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Select from '@Component/Select/Select';
  import H1 from '@Component/H/H1';
  import Button from '@Component/Button/Button';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Confirm from '@Component/Confirm/Confirm';

  export let params;

  let overlay = false;

  let mpolluxVersionInfo = Maybe.None();
  let energiatodistus = Maybe.None();

  const statuses = Object.freeze({
    NOT_STARTED: 0,
    MPOLLUX: 1,
    STARTED: 2,
    DIGEST: 3,
    SIGNATURE: 4,
    SUCCESS: 5,
    FAILURE: 6
  });

  let signatureStatus = statuses.NOT_STARTED;

  const etFuture = R.curry((apiMethod, params) =>
    R.converge(apiMethod(fetch), [R.prop('version'), R.prop('id')])(params)
  );

  Future.fork(
    e => console.error(e),
    ([info, et]) => {
      mpolluxVersionInfo = Maybe.of(info);
      energiatodistus = Maybe.of(et);
      signatureStatus = statuses.MPOLLUX;
    },
    R.compose(
      Future.parallel(5),
      R.append(R.__, [sgApi.versionInfo(fetch)]),
      etFuture(etApi.getEnergiatodistusById)
    )(params)
  );

  const signProcess = R.compose(
    Future.fork(
      _ => {
        overlay = false;
        signatureStatus = statuses.FAILURE;
      },
      _ => {
        overlay = false;
        signatureStatus = statuses.SUCCESS;
      }
    ),
    R.chain(_ => etFuture(etApi.finishSign, params)),
    R.chain(etFuture(etApi.signPdf, params)),
    R.map(R.tap(_ => (signatureStatus = statuses.SIGNATURE))),
    R.chain(sgApi.getSignature(fetch)),
    R.map(R.tap(_ => (signatureStatus = statuses.DIGEST))),
    R.chain(_ => etFuture(etApi.digest, params)),
    R.map(
      R.tap(_ => {
        overlay = true;
        signatureStatus = statuses.STARTED;
      })
    ),
    etFuture(etApi.startSign)
  );
</script>

<style type="text/postcss">

</style>

<H1 text={'Allekirjoittaminen'} />
{#if R.allPass([
  Maybe.isSome,
  R.compose(
    Maybe.isSome,
    R.chain(R.prop('allekirjoitusaika'))
  )
])(energiatodistus)}
  Energiatodistus on jo allekirjoitettu.
{:else}
  <Overlay {overlay}>
    <form slot="content" on:submit|preventDefault={() => signProcess(params)}>
      {#if signatureStatus === statuses.NOT_STARTED}
        <span>Tarkistetaan allekirjoitusohjelmiston olemassaoloa...</span>
      {:else if signatureStatus === statuses.MPOLLUX}
        <span>
          {R.compose( Maybe.orSome(''), R.map(et => `Allekirjoitetaan energiatodistusta ${et.id}`) )(energiatodistus)}
        </span>
        <Button text="Siirry allekirjoittamaan" type="submit" />
      {:else if signatureStatus === statuses.SUCCESS}
        <span>Allekirjoitus onnistui.</span>
      {:else if signatureStatus === statuses.FAILURE}
        <span>Allekirjoitus epäonnistui</span>
        <Button text="Siirry allekirjoittamaan" type="submit" />
      {:else}
        <span>Allekirjoitetaan...</span>
      {/if}
    </form>
    <div
      slot="overlay-content"
      class="flex flex-col items-center justify-center">
      <Spinner />
      <span>
        {#if signatureStatus === statuses.STARTED}
          Allekirjoitettavaa tiedostoa muodostetaan.
        {:else if signatureStatus === statuses.DIGEST || signatureStatus === statuses.SIGNATURE}
          Tiedostoa allekirjoitetaan.
        {/if}
      </span>
    </div>
  </Overlay>
{/if}
