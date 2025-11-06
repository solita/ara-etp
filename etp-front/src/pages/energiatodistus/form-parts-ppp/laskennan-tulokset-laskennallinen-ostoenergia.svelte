<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as EitherMaybe from '@Utility/either-maybe.js';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';
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
      <td class="et-table--td">TODO E-luokka</td>
      <td class="et-table--td">TODO E-luokka</td>
      {#each perusparannuspassi.vaiheet as vaihe}
        <td class="et-table--td">
          {#if Maybe.isSome(EitherMaybe.toMaybe(vaihe.tulokset['vaiheen-alku-pvm']))}
            TODO E-luokka
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
