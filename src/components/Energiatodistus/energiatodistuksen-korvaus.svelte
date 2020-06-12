<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import AsyncAutocomplete from '@Component/Autocomplete/AsyncAutocomplete';
  import Input from '@Component/Input/Input';

  import * as EtApi from './energiatodistus-api';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';

  export let model;
  export let lens;

  let korvattavaEnergiatodistusId = Maybe.None();
  let korvattavaEnergiatodistus = Maybe.None();

  let checked = false;
  let completedValue = '';

  let cancel = () => {};

  const parseEt = R.compose(
    Maybe.fromEmpty,
    R.trim
  );

  const isIdMatch = R.curry((energiatodistus, id) =>
    R.compose(
      R.equals(id),
      R.toString,
      R.prop('id')
    )(energiatodistus)
  );

  const fetchEnergiatodistus = id => {
    if (
      R.compose(
        Maybe.exists(R.identity),
        R.lift(isIdMatch)
      )(korvattavaEnergiatodistus, id)
    ) {
      return;
    }
    cancel = R.compose(
      Maybe.orSome(() => {}),
      R.map(
        R.compose(
          Future.fork(
            _ => {
              korvattavaEnergiatodistus = Maybe.None();
            },
            et => {
              model = R.set(lens, R.map(i => parseInt(i, 10), id), model);
              korvattavaEnergiatodistus = Maybe.Some(et);
            }
          ),
          EtApi.getEnergiatodistusById(fetch, 'all')
        )
      ),
      R.tap(_ => (korvattavaEnergiatodistus = Maybe.None())),
      R.tap(cancel)
    )(id);
  };

  $: korvattavaEnergiatodistusId = parseEt(completedValue);

  $: fetchEnergiatodistus(korvattavaEnergiatodistusId);
</script>

<style>

</style>

<Checkbox label={'Korvaa todistuksen'} bind:model={checked} />

{#if checked}
  <div class="flex flex-col -mx-4 mt-4" in:slide={{ duration: 200 }}>
    <div class="w-full lg:w-1/2 px-4 py-2">
      <AsyncAutocomplete
        bind:completedValue
        createFutureFn={EtApi.signed}
        id={'korvattavaenergiatodistus'}
        name={'korvattavaenergiatodistus'}
        label={'Korvattava todistus'}
        required={true}
        bind:model={korvattavaEnergiatodistusId}
        lens={R.lens(R.identity, R.identity)}
        format={Maybe.orSome('')}
        parse={parseEt}
        i18n={$_} />
    </div>
    {#each Maybe.toArray(korvattavaEnergiatodistus) as et}
      <div class="w-full px-4 py-2">
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr etp-table--tr__light">
              <th class="etp-table--th">Tunnus</th>
              <th class="etp-table--th">Etl</th>
              <th class="etp-table--th">ktl</th>
              <th class="etp-table--th">pysyvä rakennustunnus</th>
              <th class="etp-table--th">nettoala</th>
              <th class="etp-table--th">rakennuksen nimi</th>
              <th class="etp-table--th">osoite</th>
              <th class="etp-table--th">laatija</th>
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            <tr class="etp-table-tr">
              <td class="etp-table--td">{et.id}</td>
              <td class="etp-table--td">{''}</td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot.kayttotarkoitus)}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.perustiedot.rakennustunnus)}
              </td>
              <td class="etp-table--td">
                {Maybe.orSome('', et.lahtotiedot['lammitetty-nettoala'])}
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
  </div>
{/if}
