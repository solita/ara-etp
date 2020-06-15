<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import Checkbox from '@Component/Checkbox/Checkbox';
  import AsyncAutocomplete from '@Component/Autocomplete/AsyncAutocomplete';
  import Input from '@Component/Input/Input';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import * as EtApi from './energiatodistus-api';
  import * as empty from './empty';

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

  let overlay = false;

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
      Maybe.cata(
        R.compose(
          R.always(() => {}),
          R.tap(_ => (korvattavaEnergiatodistus = Maybe.None()))
        ),
        R.compose(
          Future.fork(
            _ => {
              overlay = false;
              korvattavaEnergiatodistus = Maybe.None();
            },
            et => {
              overlay = false;
              model = R.set(lens, R.map(i => parseInt(i, 10), id), model);
              korvattavaEnergiatodistus = Maybe.Some(et);
            }
          ),
          R.chain(Future.after(2000)),
          EtApi.getEnergiatodistusById(fetch, 'all'),
          R.tap(_ => {
            overlay = true;
            korvattavaEnergiatodistus = R.compose(
              R.map(R.assoc('laatija-fullname', Maybe.None())),
              Maybe.Some
            )(empty.energiatodistus2018());
          })
        )
      ),
      R.tap(cancel)
    )(id);
  };

  const resetKorvaus = () => {
    model = R.set(lens, Maybe.None(), model);
    korvattavaEnergiatodistus = Maybe.None();
    korvattavaEnergiatodistusId = Maybe.None();
  };

  $: korvattavaEnergiatodistusId = parseEt(completedValue);

  $: fetchEnergiatodistus(korvattavaEnergiatodistusId);

  $: checked === false && resetKorvaus();
</script>

<style>

</style>

<Checkbox label={'Korvaa todistuksen'} bind:model={checked} />

{#if checked}
  <div
    class="flex flex-col -mx-4 mt-4"
    transition:slide|local={{ duration: 200 }}>
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
        reject={Maybe.fromNull(model.id)}
        i18n={$_} />
    </div>
    {#each Maybe.toArray(korvattavaEnergiatodistus) as et}
      <div
        class="w-full px-4 py-2 relative"
        transition:slide|local={{ duration: 200 }}>
        <Overlay {overlay}>
          <div slot="content" class="w-full">
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
                  <td class="etp-table--td">
                    {R.defaultTo(Maybe.get(korvattavaEnergiatodistusId), et.id)}
                  </td>
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
          <div slot="overlay-content">
            <Spinner />
          </div>
        </Overlay>
      </div>
    {/each}
  </div>
{/if}
