<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import { loc } from 'svelte-spa-router';

  import { _ } from '@Language/i18n';

  import * as ViestiApi from '@Pages/viesti/viesti-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Viestiketju from '@Pages/viesti/viestiketju';
  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');

  export let params;

  let resources = Maybe.None();
  let overlay = true;

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );

        announceError(msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(4, {
        whoami: kayttajaApi.whoami,
        ketjut: ViestiApi.getEnergiatodistusKetjut(params.id),
        vastaanottajaryhmat: ViestiApi.vastaanottajaryhmat,
        kasittelijat: ViestiApi.getKasittelijat
      })
    );
  };

  $: load(params);

  const submitKasitelty = (ketjuId, kasitelty) => {
    updateKetju(ketjuId, {
      kasitelty: kasitelty
    });
  };

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `viesti.all.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        announceError(msg);
        overlay = false;
      },
      _ => {
        announceSuccess(i18n(`viesti.all.messages.update-success`));
        overlay = false;
        load(params);
      }
    ),
    R.tap(() => {
      overlay = true;
    }),
    ViestiApi.putKetju(fetch)
  );
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami, vastaanottajaryhmat, kasittelijat }}
      <div class="flex justify-between">
        <H1 text={i18n('energiatodistus.viestit.title')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n('viesti.all.new-viesti')}
            href={'#' + $loc.location + '/new'} />
        </div>
      </div>
      {#if ketjut.length === 0}
        <span>{i18n('viesti.all.no-messages')}</span>
      {/if}
      <div class="my-6">
        {#each ketjut as ketju}
          <Viestiketju
            {ketju}
            {whoami}
            {vastaanottajaryhmat}
            {kasittelijat}
            {submitKasitelty} />
        {/each}
      </div>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
