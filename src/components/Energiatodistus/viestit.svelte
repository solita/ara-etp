<script>
  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import { push } from '@Component/Router/router';

  import * as ViestiApi from '@Component/viesti/viesti-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Viestiketju from '@Component/viesti/viestiketju';

  const i18n = $_;

  export let params;

  let resources = Maybe.None();
  let overlay = true;

  const nextPageCallback = nextPage => push(`#/viesti/all?page=${nextPage}`);

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

        flashMessageStore.add('Energiatodistus', 'error', msg);
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
        flashMessageStore.add('Energiatodistus', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'Energiatodistus',
          'success',
          i18n(`viesti.all.messages.update-success`)
        );
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
            href="#/viesti/new" />
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
