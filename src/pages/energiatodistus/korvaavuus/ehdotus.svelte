<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link.svelte';
  import Address from '@Pages/energiatodistus/address.svelte';

  import * as EnergiatodistusApi from '../energiatodistus-api';
  import * as ET from '../energiatodistus-utils';

  export let energiatodistus;
  export let postinumerot;
  export let checked;
  export let whoami;
  export let dirty;

  const i18n = $_;
  const i18nRoot = 'energiatodistus.korvaavuus';

  let loading = false;
  let korvattavat = [];

  const loadKorvattavat = energiatodistus => {
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
      EnergiatodistusApi.korvattavat(
        R.pick(
          ['rakennustunnus', 'postinumero', 'katuosoite-fi', 'katuosoite-sv'],
          energiatodistus.perustiedot
        )
      )
    );
  };

  $: if (
    ET.isDraft(energiatodistus) &&
    Maybe.isNone(energiatodistus['korvattu-energiatodistus-id']) &&
    Kayttajat.isLaatija(whoami) &&
    !checked
  ) {
    loadKorvattavat(energiatodistus);
  } else {
    korvattavat = [];
  }
</script>

<style type="text/postcss">
  h3 {
    @apply text-primary uppercase font-bold text-sm;
  }
  h3 span {
    @apply lowercase text-lg font-normal;
  }
</style>

{#if loading}
  <Spinner />
{/if}

{#if !R.isEmpty(korvattavat)}
  <div
    class="my-4 border-primary border-2 border-opacity-15 rounded-md p-4 shadow-md">
    <h3>
      <span class="font-icon">info_outline</span>
      {i18n(i18nRoot + '.header.ehdotus')}
    </h3>
    <p class="my-2">{i18n(i18nRoot + '.ehdotus.info-p1')}</p>
    <p class="my-2">{@html i18n(i18nRoot + '.ehdotus.info-p2')}</p>
    <p class="my-2">
      {i18n(i18nRoot + '.ehdotus.info-p3-1')}
      <Link
        on:click={_ => {
          checked = true;
          dirty = true;
        }}
        text={i18n(i18nRoot + '.ehdotus.info-p3-2')} />
      {i18n(i18nRoot + '.ehdotus.info-p3-3')}
    </p>
    <div class="flex flex-col -mx-4 mt-2">
      <div
        class="w-full px-4 py-4 relative"
        transition:slide|local={{ duration: 200 }}>
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr etp-table--tr__light">
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.tunnus')}
              </th>
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.ktl')}
              </th>
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.rakennustunnus')}
              </th>
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.nimi')}
              </th>
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.osoite')}
              </th>
              <th class="etp-table--th">
                {i18n(i18nRoot + '.table.laatija')}
              </th>
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each korvattavat as korvattava}
              <tr
                class="etp-table-tr etp-table--tr__link"
                on:click={() => {
                  energiatodistus = R.assoc(
                    'korvattu-energiatodistus-id',
                    Maybe.Some(korvattava.id),
                    energiatodistus
                  );
                  dirty = true;
                }}>
                <td class="etp-table--td">
                  {korvattava.id}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', korvattava.perustiedot.kayttotarkoitus)}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', korvattava.perustiedot.rakennustunnus)}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', korvattava.perustiedot.nimi)}
                </td>
                <td class="etp-table--td">
                  <Address energiatodistus={korvattava} {postinumerot} />
                </td>
                <td class="etp-table--td">
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
