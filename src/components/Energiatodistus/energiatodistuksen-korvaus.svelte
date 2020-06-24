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
  export let initialEnergiatodistusId = Maybe.None();

  let cancel = () => {};
  let completedValue = '';
  let overlay = false;

  let korvattavaEnergiatodistusId = Maybe.isSome(initialEnergiatodistusId)
    ? initialEnergiatodistusId
    : Maybe.None();
  let korvattavaEnergiatodistus = Maybe.None();

  let checked = Maybe.isSome(korvattavaEnergiatodistusId);

  const energiatodistusMatchId = R.curry((energiatodistus, id) =>
    R.compose(
      R.equals(id),
      R.prop('id')
    )(energiatodistus)
  );

  const parseEt = R.compose(
    Maybe.fromFalsy,
    value => parseInt(value, 10)
  );

  const fetchEnergiatodistus = id => {
    console.log(id);
    if (
      R.compose(
        Maybe.exists(R.identity),
        R.lift(energiatodistusMatchId)
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
          R.chain(Future.after(200)),
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

  $: checked
    ? (model = R.set(lens, korvattavaEnergiatodistusId, model))
    : (model = R.set(lens, Maybe.None(), model));

  $: korvattavaEnergiatodistusId = R.compose(
    Maybe.orElse(korvattavaEnergiatodistusId),
    parseEt
  )(completedValue);

  $: fetchEnergiatodistus(korvattavaEnergiatodistusId);
</script>

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
        class="w-full px-4 py-4 relative"
        transition:slide|local={{ duration: 200 }}>
        <Overlay {overlay}>
          <div slot="content" class="w-full">
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
                <tr class="etp-table-tr">
                  <td class="etp-table--td">
                    {R.defaultTo(Maybe.getOrElse(' ', korvattavaEnergiatodistusId), et.id)}
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
