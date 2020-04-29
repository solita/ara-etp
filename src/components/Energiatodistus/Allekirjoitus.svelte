<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import * as Future from '@Utility/future-utils';
  import * as etApi from './energiatodistus-api';

  import { currentUserStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import Select from '@Component/Select/Select';
  import H1 from '@Component/H/H1';
  import Button from '@Component/Button/Button';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Confirm from '@Component/Confirm/Confirm';

  export let params;

  const mpolluxUrl = 'https://localhost:53952';
  const mpolluxVersionUrl = `${mpolluxUrl}/version`;
  const mpolluxSignUrl = `${mpolluxUrl}/sign`;

  let mpolluxVersionInfo = Maybe.None();
  let energiatodistus = Maybe.None();

  Future.fork(
    e => console.error(e),
    ([info, et]) => {
      mpolluxVersionInfo = Maybe.of(info);
      energiatodistus = Maybe.of(et);
    },
    R.compose(
      Future.parallel(5),
      R.append(R.__, [Fetch.fetchFromUrl(fetch, mpolluxVersionUrl)]),
      R.converge(etApi.getEnergiatodistusById(fetch), [
        R.prop('version'),
        R.prop('id')
      ])
    )(params)
  );

  let overlay = false;
  let signing = false;
  let successful = Maybe.None();

  const sign = R.compose(
    R.chain(response =>
      response.status === 'failed'
        ? Future.reject(response)
        : R.compose(
            Future.resolve,
            R.pick(['signature', 'chain'])
          )(response)
    ),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', mpolluxSignUrl)),
    R.assoc('content', R.__, {
      version: '1.1',
      selector: {
        keyusages: ['nonrepudiation'],
        keyalgorithms: ['rsa']
      },
      contentType: 'data',
      hashAlgorithm: 'SHA256',
      signatureType: 'signature'
    })
  );

  const signProcess = R.compose(
    Future.fork(
      _ => {
        signing = false;
        overlay = false;
        successful = Maybe.of(false);
      },
      _ => {
        signing = false;
        overlay = false;
        successful = Maybe.of(true);
      }
    ),
    R.chain(etApi.signEnergiatodistus(fetch, params.version, params.id)),
    R.chain(sign),
    R.map(R.tap(_ => (signing = true))),
    R.converge(etApi.getEnergiatodistusDigestById(fetch), [
      R.prop('version'),
      R.prop('id')
    ]),
    R.map(R.tap(_ => (overlay = true)))
  );
</script>

<style type="text/postcss">

</style>

<Confirm let:confirm={confirmAction}>
  <div>
    <H1 text={'Allekirjoittaminen'} />

    <Overlay {overlay}>
      <form
        slot="content"
        on:submit|preventDefault={() => confirmAction(signProcess, params)}>
        {#if R.all(Maybe.isSome, [
          mpolluxVersionInfo,
          energiatodistus
        ]) && !Maybe.isSome(successful)}
          <Button text="Siirry allekirjoittamaan" type="submit" />
        {:else if Maybe.orSome(false, successful)}
          Allekirjoitus onnistui.
        {:else if !Maybe.isSome(mpolluxVersionInfo)}
          Tarkastetaan allekirjoituspalvelun olemassaoloa...
        {:else}Allekirjoitus epäonnistui{/if}
      </form>
      <div
        slot="overlay-content"
        class="flex flex-col items-center justify-center">
        <Spinner />
        <span>
          {#if !signing}
            Allekirjoitettavaa tiedostoa muodostetaan.
          {:else}Tiedostoa allekirjoitetaan{/if}
        </span>
      </div>
    </Overlay>
  </div>
</Confirm>
