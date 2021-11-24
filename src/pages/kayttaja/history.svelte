<script>
  import { _ } from '@Language/i18n';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';

  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as GeoApi from '@Utility/api/geo-api';
  import * as LaskutusApi from '@Utility/api/laskutus-api';
  import { flashMessageStore } from '@/stores';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H2 from '@Component/H/H2';
  import KayttajaHistoryTable from './history-table';
  import LaatijaHistoryTable from '@Pages/laatija/history-table';

  export let params;

  $: id = params.id;

  const i18n = $_;
  let overlay = true;
  let resources = Maybe.None();

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'ohje.viewer.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('ohje', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(8, {
        roolit: kayttajaApi.roolit,
        kayttaja: kayttajaApi.getKayttajaById(id),
        kayttajaHistory: kayttajaApi.getKayttajaHistory(id),
        laatijaHistory: laatijaApi.getLaatijaHistory(id),
        patevyydet: laatijaApi.patevyydet,
        toimintaalueet: GeoApi.toimintaalueet,
        countries: GeoApi.countries,
        laskutuskielet: LaskutusApi.laskutuskielet
      })
    );
  }
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3 flex flex-col space-y-8">
    {#each Maybe.toArray(resources) as { kayttaja, kayttajaHistory, laatijaHistory, roolit, patevyydet, toimintaalueet, countries, laskutuskielet }}
      <div>
        <H2 text={i18n('kayttaja.muutoshistoria.kayttajatiedot')} />

        <KayttajaHistoryTable history={R.reverse(kayttajaHistory)} {roolit} />
      </div>
      {#if Kayttajat.isLaatija(kayttaja)}
        <div>
          <H2 text={i18n('kayttaja.muutoshistoria.laatijatiedot')} />

          <LaatijaHistoryTable
            history={R.reverse(laatijaHistory)}
            {patevyydet}
            {toimintaalueet}
            {countries}
            {laskutuskielet} />
        </div>
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
