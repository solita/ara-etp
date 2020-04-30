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
      R.converge(etApi.getEnergiatodistusById(fetch), [
        R.prop('version'),
        R.prop('id')
      ])
    )(params)
  );

  const signProcess = R.compose(
    Future.fork(
      _ => (signatureStatus = statuses.FAILURE),
      _ => (signatureStatus = statuses.SUCCESS)
    ),
    R.map(
      R.tap(_ => {
        overlay = false;
      })
    ),
    R.chain(etApi.signEnergiatodistus(fetch, params.version, params.id)),
    R.map(R.tap(_ => (signatureStatus = statuses.SIGNATURE))),
    R.chain(sgApi.getSignature(fetch)),
    R.map(R.tap(_ => (signatureStatus = statuses.DIGEST))),
    R.converge(etApi.getEnergiatodistusDigestById(fetch), [
      R.prop('version'),
      R.prop('id')
    ]),
    R.tap(_ => {
      overlay = true;
      signatureStatus = statuses.STARTED;
    })
  );
</script>

<style type="text/postcss">

</style>

<H1 text={'Allekirjoittaminen'} />

<Overlay {overlay}>
  <form slot="content" on:submit|preventDefault={() => signProcess(params)}>
    {#if signatureStatus === statuses.NOT_STARTED}
      Tarkistetaan allekirjoitusohjelmiston olemassaoloa...
    {:else if signatureStatus === statuses.MPOLLUX}
      <span>
        {R.compose( Maybe.orSome(''), R.map(et => `Allekirjoitetaan energiatodistusta ${et.id}`) )(energiatodistus)}
      </span>
      <Button text="Siirry allekirjoittamaan" type="submit" />
    {:else if signatureStatus === statuses.SUCCESS}
      Allekirjoitus onnistui.
    {:else if signatureStatus === statuses.FAILURE}
      Allekirjoitus epäonnistui
    {:else}Allekirjoitetaan...{/if}
  </form>
  <div slot="overlay-content" class="flex flex-col items-center justify-center">
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
