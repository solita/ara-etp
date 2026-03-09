<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EitherMaybe from '@Utility/either-maybe.js';
  import * as Future from '@Utility/future-utils';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as PPPUtils from './ppp-utils';

  import H4 from '@Component/H/H4';
  import { _ } from '@Language/i18n';
  import Input from '@Pages/energiatodistus/Input';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;

  const etEnergiamuodotFromPppVaihe = vaihe =>
    R.compose(
      R.fromPairs,
      R.map(({ pppLaskennallinenEnergiamuoto, etEnergiamuoto }) => [
        etEnergiamuoto,
        vaihe[pppLaskennallinenEnergiamuoto]
      ])
    )(PPPUtils.energiamuodot);

  /**
   * Returns Maybe of [apiParams, Future] where the Future resolves to the
   * e-luokka letter (A–G). None when input data is insufficient.
   */
  const fetchVaiheEluokka = (
    versio,
    kayttotarkoitus,
    nettoala,
    vaiheEnergiamuodot
  ) => {
    const eLuku = ETUtils.eluku(versio, nettoala, vaiheEnergiamuodot);

    return R.map(
      params => [
        params,
        R.compose(
          R.map(R.prop('e-luokka')),
          R.apply(api.getEluokka(fetch, versio))
        )(params)
      ],
      Maybe.toMaybeList([kayttotarkoitus, EitherMaybe.toMaybe(nettoala), eLuku])
    );
  };

  // Vaihe index → e-luokka letter (A–G) retrieved from the e-luokka API.
  // Displayed in the template.
  let eluokkaPerVaihe = {};
  // Vaihe index → [kayttotarkoitus, nettoala, eLuku] from the last API call.
  // Used to skip redundant fetches when reactive updates fire.
  let eluokkaParamsPerVaihe = {};

  const updateEluokat = (et, vaiheet) => {
    vaiheet.forEach((vaihe, vaiheIndex) => {
      const hasAlkuPvm = Maybe.isSome(
        EitherMaybe.toMaybe(vaihe.tulokset['vaiheen-alku-pvm'])
      );

      // Attempt to build the API call for this vaihe.
      // Returns None if alku-pvm is missing or data is insufficient for e-luku.
      const result = hasAlkuPvm
        ? fetchVaiheEluokka(
            et.versio,
            et.perustiedot.kayttotarkoitus,
            et.lahtotiedot['lammitetty-nettoala'],
            etEnergiamuodotFromPppVaihe(vaihe.tulokset)
          )
        : Maybe.None();

      Maybe.fold(
        // None: clear any previously fetched e-luokka for this vaihe
        () => {
          eluokkaPerVaihe = R.dissoc(String(vaiheIndex), eluokkaPerVaihe);
          eluokkaParamsPerVaihe = R.dissoc(
            String(vaiheIndex),
            eluokkaParamsPerVaihe
          );
        },
        // Some: fetch e-luokka, but only if API params changed since last call
        ([params, eluokkaFuture]) => {
          if (!R.equals(params, eluokkaParamsPerVaihe[vaiheIndex])) {
            eluokkaParamsPerVaihe[vaiheIndex] = params;
            Future.fork(
              _ => {},
              eluokka => {
                eluokkaPerVaihe[vaiheIndex] = eluokka;
                eluokkaPerVaihe = eluokkaPerVaihe; // trigger Svelte reactivity
              },
              eluokkaFuture
            );
          }
        },
        result
      );
    });
  };

  $: updateEluokat(energiatodistus, perusparannuspassi.vaiheet);
</script>

<H4
  text={$_(
    'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.header'
  )} />

<p>
  {$_('perusparannuspassi.laskennan-tulokset.info-kirjaa-arvot')}
</p>

<table class="et-table">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th-left-aligned"
        >{$_(
          'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.header'
        )}</th>
      <th class="et-table--th et-table--th-right-aligned"
        >{$_(
          'perusparannuspassi.laskennan-tulokset.lahtotilanne-kwh-vuosi'
        )}</th>

      {#each perusparannuspassi.vaiheet as vaihe}
        <th class="et-table--th et-table--th-right-aligned"
          >{PPPUtils.formatVaiheHeading(
            `${$_('perusparannuspassi.laskennan-tulokset.vaihe')} ${vaihe['vaihe-nro']}`,
            $_('perusparannuspassi.laskennan-tulokset.kwh-per-vuosi'),
            vaihe.tulokset['vaiheen-alku-pvm'],
            $_('perusparannuspassi.laskennan-tulokset.ei-aloitusvuotta')
          )}</th>
      {/each}
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each PPPUtils.energiamuodot as { etEnergiamuoto, pppLaskennallinenEnergiamuoto }}
      <tr class="et-table--tr">
        <td class="et-table--td"
          >{$_(
            'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.' +
              etEnergiamuoto
          )}</td>

        <td class="et-table--td"
          >{EitherMaybe.orSome(
            0,
            energiatodistus.tulokset['kaytettavat-energiamuodot'][
              etEnergiamuoto
            ]
          )}</td>

        {#each perusparannuspassi.vaiheet as vaihe}
          <td class="et-table--td">
            <Input
              {schema}
              center={true}
              bind:model={perusparannuspassi}
              compact={true}
              i18nRoot="perusparannuspassi"
              disabled={R.compose(
                R.not,
                Maybe.isSome,
                EitherMaybe.toMaybe
              )(vaihe['tulokset']['vaiheen-alku-pvm'])}
              path={[
                'vaiheet',
                vaihe['vaihe-nro'] - 1,
                'tulokset',
                pppLaskennallinenEnergiamuoto
              ]} />
          </td>
        {/each}
      </tr>
    {/each}
    <tr class="et-table--tr">
      <td class="et-table--td">{$_('energiatodistus.tulokset.e-luokka')}</td>
      <td class="et-table--td"
        >{Maybe.orSome('', energiatodistus.tulokset['e-luokka'])}</td>
      {#each perusparannuspassi.vaiheet as vaihe, vaiheIndex}
        <td class="et-table--td">
          {#if Maybe.isSome(EitherMaybe.toMaybe(vaihe.tulokset['vaiheen-alku-pvm']))}
            {R.defaultTo('', eluokkaPerVaihe[vaiheIndex])}
          {/if}
        </td>
      {/each}
    </tr>
    <tr class="et-table--tr">
      <td class="et-table--td">{$_('energiatodistus.tulokset.e-luku')}</td>
      <td class="et-table--td">
        {Maybe.orSome(
          '',
          ETUtils.eluku(
            energiatodistus.versio,
            energiatodistus.lahtotiedot['lammitetty-nettoala'],
            energiatodistus.tulokset['kaytettavat-energiamuodot']
          )
        )}</td>

      {#each perusparannuspassi.vaiheet as vaihe}
        <td class="et-table--td">
          {#if Maybe.isSome(EitherMaybe.toMaybe(vaihe.tulokset['vaiheen-alku-pvm']))}
            {Maybe.orSome(
              '',
              ETUtils.eluku(
                energiatodistus.versio,
                energiatodistus.lahtotiedot['lammitetty-nettoala'],
                etEnergiamuodotFromPppVaihe(vaihe.tulokset)
              )
            )}
          {/if}
        </td>
      {/each}
    </tr>
  </tbody>
</table>
