<script>
  import { slide } from 'svelte/transition';
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import HR from '@Component/HR/HR';
  import H2 from '@Component/H/H2';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Input from '@Component/Input/Input';
  import Spinner from '@Component/Spinner/Spinner';

  import * as EtApi from './energiatodistus-api';

  import * as Korvaavuus from './energiatodistuksen-korvaavuus';
  import * as ET from './energiatodistus-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Parsers from '@Utility/parsers';

  export let energiatodistus;
  export let whoami;
  export let error = Maybe.None();

  const enabled =
    Kayttajat.isLaatija(whoami.rooli) && R.propEq('tila-id', ET.tila.draft, energiatodistus) ||
    Kayttajat.isPaakayttaja(whoami.rooli) &&
      !ET.isTilaInTilat([ET.tila.draft, ET.tila.deleted])(energiatodistus);

  const lens = R.lensProp('korvattu-energiatodistus-id');

  let cancel = () => {};
  let overlay = false;
  let query = R.view(lens, energiatodistus);
  let korvattavaEnergiatodistus = Maybe.None();
  let checked = Maybe.isSome(R.view(lens, energiatodistus));

  const parseId = R.compose(
    Maybe.fromFalsy,
    value => parseInt(value, 10)
  );

  const setKorvattavaEnergiatodistus = id => {
    if (!R.equals(R.view(lens, energiatodistus), id)) {
      energiatodistus = R.set(lens, id, energiatodistus);
    }
  }

  const fetchKorvattavaEnergiatodistus = id => {
    cancel = R.compose(
      Future.fork(
        _ => {
          overlay = false;
          korvattavaEnergiatodistus = Maybe.None();
        },
        et => {
          overlay = false;
          setKorvattavaEnergiatodistus(Maybe.Some(et.id))
          korvattavaEnergiatodistus = Maybe.Some(et);
        }
      ),
      R.chain(Future.after(400)),
      EtApi.getEnergiatodistusById(fetch, 'all'),
      R.tap(_ => {
        overlay = true;
      }),
      R.tap(cancel))(id);
  };

  R.forEach(fetchKorvattavaEnergiatodistus, R.view(lens, energiatodistus));

  $: if (enabled && !overlay) {
    if (checked) {
      setKorvattavaEnergiatodistus(korvattavaEnergiatodistus.map(R.prop('id')));
    } else {
      setKorvattavaEnergiatodistus(Maybe.None());
    }
  }

  const searchKorvattavaEnergiatodistus = R.compose(
    Maybe.cata(_ => {
        korvattavaEnergiatodistus = Maybe.None();
      },
      fetchKorvattavaEnergiatodistus),
    R.chain(parseId)
  );

  const updateQuery = event => {
    query = Parsers.optionalString(event.target.value);
    searchKorvattavaEnergiatodistus(query)
  }

  $: error = R.compose(
      R.filter(R.always(R.view(lens, energiatodistus).isSome())),
      R.chain(korvattava =>
        Korvaavuus.isSame(korvattava, energiatodistus) ? Maybe.Some('is-same') :
        !Korvaavuus.isValidState(korvattava, energiatodistus) ? Maybe.Some('invalid-tila') :
        Korvaavuus.hasOtherKorvaaja(korvattava, energiatodistus) ? Maybe.Some('already-replaced') :
        !Korvaavuus.isValidLocation(korvattava, energiatodistus) ? Maybe.Some('invalid-location') :
          Maybe.None()))
    (korvattavaEnergiatodistus);

</script>

<style type="text/postcss">
  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }

  .error-icon {
    @apply text-error;
  }
</style>

{#if checked || enabled}
  <H2 text={$_('energiatodistus.korvaavuus.header')} />

  <Checkbox label={'Korvaa todistuksen'} bind:model={checked} disabled={!enabled} />

  {#if checked}
    <div
        class="flex flex-col -mx-4 mt-4"
        transition:slide|local={{ duration: 200 }}>
      {#if enabled}
        <div class="w-full lg:w-1/2 px-4 py-2" on:input={updateQuery}>
          <Input
              model={query}
              id={'korvattavaenergiatodistus'}
              name={'korvattavaenergiatodistus'}
              label={'Valitse korvattava todistus'}
              lens={R.lens(R.identity, R.identity)}
              format={Maybe.orSome('')}
              parse={Parsers.optionalString}
              search={true}
              i18n={$_}/>
        </div>
      {/if}
      {#if !overlay}
        {#each Maybe.toArray(korvattavaEnergiatodistus) as et}
          <div class="w-full px-4 py-4 relative"
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
              <tr class="etp-table-tr">
                <td class="etp-table--td">
                  {et.id}
                </td>
                <td class="etp-table--td">
                  {Maybe.orSome('', et.tulokset['e-luokka'])}
                </td>
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
            {#each Maybe.toArray(error) as key}
              <div class="error-label">
                <span class="font-icon error-icon">error</span>
                {$_('energiatodistus.korvaavuus.validation.' + key)}
              </div>
            {/each}
          </div>
        {/each}
        {#if query.isSome() && korvattavaEnergiatodistus.isNone() && !overlay}
          <div class="w-full">
            <div class="error-label px-4">
              <span class="font-icon error-icon">error</span>
              {$_('energiatodistus.korvaavuus.validation.not-found')}
            </div>
          </div>
        {/if}
      {:else}
        <Spinner/>
      {/if}
    </div>
  {/if}

  <HR />
{/if}