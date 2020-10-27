<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';

  import { push } from '@Component/Router/router';
  import { _ } from '@Language/i18n';

  import Spinner from '@Component/Spinner/Spinner';
  import H2 from '@Component/H/H2';
  import HR from '@Component/HR/HR';

  import * as EtApi from './energiatodistus-api';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';

  export let korvaavaEnergiatodistusId = Maybe.None();

  let overlay = false;

  let korvaavaEnergiatodistus = Maybe.None();

  const fetchEnergiatodistus = Maybe.cata(
    _ => {
      korvaavaEnergiatodistus = Maybe.None()
    },
    R.compose(
      Future.fork(
        _ => {
          overlay = false;
          korvaavaEnergiatodistus = Maybe.None();
        },
        et => {
          overlay = false;
          korvaavaEnergiatodistus = Maybe.Some(et);
        }
      ),
      R.chain(Future.after(200)),
      EtApi.getEnergiatodistusById(fetch, 'all'),
      R.tap(_ => {
        overlay = true;
      })
    ));

  $: fetchEnergiatodistus(korvaavaEnergiatodistusId);
</script>

<style type="text/postcss">

</style>

{#if korvaavaEnergiatodistusId.isSome()}
  <H2 text={$_('energiatodistus.korvaava.header')}/>
  <div
      class="flex flex-col -mx-4 mt-4"
      transition:slide|local={{ duration: 200 }}>

    {#if !overlay}
      {#each Maybe.toArray(korvaavaEnergiatodistus) as et}
        <div
            class="w-full px-4 py-4 relative"
            transition:slide|local={{ duration: 200 }}>
          <table class="etp-table">
            <thead class="etp-table--thead">
            <tr class="etp-table--tr etp-table--tr__light">
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.tunnus')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.etl')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.ktl')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.rakennustunnus')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.nettoala')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.nimi')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.osoite')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.korvaavuus.laatija')}
              </th>
            </tr>
            </thead>
            <tbody class="etp-table--tbody">
            <tr
                class="etp-table-tr etp-table--tr__link"
                on:click={() => push('#/energiatodistus/' + et.versio + '/' + et.id)}>
              <td class="etp-table--td">
                {R.defaultTo(Maybe.getOrElse(' ', korvaavaEnergiatodistusId), et.id)}
              </td>
              <td class="etp-table--td">{''}</td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot.kayttotarkoitus)}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot.rakennustunnus)}
              </td>
              <td class="etp-table--td">
                {EM.orSome('', et.lahtotiedot['lammitetty-nettoala'])}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot.nimi)}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot['katuosoite-fi'])}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et['laatija-fullname'])}
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      {/each}
    {:else}
      <Spinner/>
    {/if}
  </div>
  <HR/>
{/if}