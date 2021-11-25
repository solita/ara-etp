<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Postinumerot from '@Component/address/postinumero-fi';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link.svelte';

  import * as EnergiatodistusApi from '../energiatodistus-api';
  import * as ET from '../energiatodistus-utils';


  export let energiatodistus;
  export let postinumerot;
  export let checked;

  const i18n = $_;
  const i18nRoot = 'energiatodistus'

  let loading = false;
  let korvattavat = [];

  const loadKorvattavat = id => {
    loading = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            i18nRoot + '.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('energiatodistus', 'error', msg);
        loading = false;
      },
      response => {
        korvattavat = response;
        loading = false;
      },
      EnergiatodistusApi.korvattavat(id)
    );
  };

  $: if (
    ET.isDraft(energiatodistus) &&
    Maybe.isNone(energiatodistus['korvattu-energiatodistus-id']) &&
    !checked) {

    loadKorvattavat(energiatodistus.id);
  } else {
    korvattavat = [];
  }
</script>

<style type='text/postcss'>
  h3 {
    @apply text-primary uppercase font-bold text-sm;
  }
  h3 span {
    @apply lowercase text-lg font-normal;
  }
  address {
    @apply not-italic;
  }
</style>

{#if loading}
  <Spinner />
{/if}

{#if !R.isEmpty(korvattavat)}
  <div class='my-4 border-primary border-2 border-opacity-15 rounded-md p-4 shadow-md'>
    <h3><span class='font-icon'>info_outline</span> Ehdotus korvattavaksi energiatodistukseksi</h3>
    <p class='my-2'>Järjestelmä on löytänyt energiatodistuksia, jotka voivat liittyä samaan rakennukseen ja käyttötarkoitukseen.</p>
    <p class='my-2'>Valitse näistä korvattava energiatodistus tai jos mitään todistusta ei korvata niin tämän kohdan voi ohittaa.</p>
    <p class='my-2'>Jos halutaan korvata jokin muu niin valitse kohta <Link on:click={_ => { checked = true}} text='Korvaa todistuksen'/> ja syötä korvattava todistustunnus käsin.</p>
    <div class='flex flex-col -mx-4 mt-2'>
      <div
        class='w-full px-4 py-4 relative'
        transition:slide|local={{ duration: 200 }}>

        <table class='etp-table'>
          <thead class='etp-table--thead'>
          <tr class='etp-table--tr etp-table--tr__light'>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.tunnus')}
            </th>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.ktl')}
            </th>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.rakennustunnus')}
            </th>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.nimi')}
            </th>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.osoite')}
            </th>
            <th class='etp-table--th'>
              {$_('energiatodistus.korvaavuus.table.laatija')}
            </th>
          </tr>
          </thead>
          <tbody class='etp-table--tbody'>
          {#each korvattavat as korvattava}
            <tr
              class='etp-table-tr etp-table--tr__link'
              on:click={() => { energiatodistus = R.assoc('korvattu-energiatodistus-id', Maybe.Some(korvattava.id), energiatodistus)}}>
              <td class='etp-table--td'>
                {korvattava.id}
              </td>
              <td class='etp-table--td'>
                {Maybe.orSome('', korvattava.perustiedot.kayttotarkoitus)}
              </td>
              <td class='etp-table--td'>
                {Maybe.orSome('', korvattava.perustiedot.rakennustunnus)}
              </td>
              <td class='etp-table--td'>
                {Maybe.orSome('', korvattava.perustiedot.nimi)}
              </td>
              <td class='etp-table--td'>
                <address>
                  {Maybe.orSome('', korvattava.perustiedot['katuosoite-fi'])}
                  <span class='whitespace-no-wrap'>
                {Maybe.fold(
                  '',
                  Postinumerot.formatPostinumero(postinumerot, $locale),
                  korvattava.perustiedot.postinumero
                )}
              </span>
                </address>
              </td>
              <td class='etp-table--td'>
                {Maybe.orSome('', korvattava['laatija-fullname'])}
              </td>
            </tr>
          {/each}
          </tbody>
        </table>
      </div>
    </div>
  </div>
{/if}
