<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EitherMaybe from '@Utility/either-maybe.js';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';

  import H3 from '@Component/H/H3';
  import H4 from '@Component/H/H4';
  import { _ } from '@Language/i18n';
  import Input from '@Pages/energiatodistus/Input';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;

  const laskennallisetOstoenergiat = [
    {
      ppp_energiamuoto: 'ostoenergian-tarve-kaukolampo',
      et_energiamuoto: 'kaukolampo'
    },
    { ppp_energiamuoto: 'ostoenergian-tarve-sahko', et_energiamuoto: 'sahko' },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-uusiutuvat-pat',
      et_energiamuoto: 'uusiutuva-polttoaine'
    },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-fossiiliset-pat',
      et_energiamuoto: 'fossiilinen-polttoaine'
    },
    {
      ppp_energiamuoto: 'ostoenergian-tarve-kaukojaahdytys',
      et_energiamuoto: 'kaukojaahdytys'
    }
  ];

  const toteutuneetOstoenergiat = [
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-kaukolampo',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukolampo-vuosikulutus'
        ]
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-sahko',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kokonaissahko-vuosikulutus'
        ]
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-uusiutuvat-pat',
      et_energiamuoto: _ => Either.Right(Maybe.None())
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-fossiiliset-pat',
      et_energiamuoto: _ => Either.Right(Maybe.None())
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-kaukojaahdytys',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukojaahdytys-vuosikulutus'
        ]
    }
  ];

  const etEnergiamuodotFromPppVaihe = vaihe =>
    R.compose(
      R.fromPairs,
      R.map(({ ppp_energiamuoto, et_energiamuoto }) => [
        et_energiamuoto,
        vaihe[ppp_energiamuoto]
      ])
    )(laskennallisetOstoenergiat);
</script>

<div class="flex flex-col gap-6">
  <H3 text={$_('perusparannuspassi.laskennan-tulokset.header')} />

  <H4 text={$_('perusparannuspassi.laskennan-tulokset.vaiheistus.header')} />

  <p>
    {$_('perusparannuspassi.laskennan-tulokset.vaiheistus.info')}
    <span class="font-bold"
      >{$_(
        'perusparannuspassi.laskennan-tulokset.vaiheistus.info-aloitusvuosi'
      )}</span>
    .
  </p>

  <table class="et-table overflow-x-auto md:overflow-x-hidden w-auto">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells"
          >{$_('perusparannuspassi.laskennan-tulokset.vaihe')}</th>
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells"
          >{$_('perusparannuspassi.laskennan-tulokset.aloitusvuosi')}</th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each perusparannuspassi.vaiheet as vaihe}
        <tr class="et-table--tr">
          <td class="et-table--td">Vaihe {vaihe['vaihe-nro']}</td>
          <td class="et-table--td">
            <Input
              {schema}
              center={false}
              bind:model={perusparannuspassi}
              compact={true}
              i18nRoot="perusparannuspassi"
              path={[
                'vaiheet',
                vaihe['vaihe-nro'] - 1,
                'tulokset',
                'vaiheen-alku-pvm'
              ]} />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>

  <H4
    text={$_(
      'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.header'
    )} />

  <p>
    {$_('perusparannuspassi.laskennan-tulokset.info-before-table')}
  </p>

  <table class="et-table">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned"
          >{$_(
            'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.header'
          )}</th>
        <th class="et-table--th et-table--th-right-aligned"
          >{$_('perusparannuspassi.laskennan-tulokset.lahtotilanne')}</th>

        {#each perusparannuspassi.vaiheet as vaihe}
          <th class="et-table--th et-table--th-right-aligned"
            >{$_('perusparannuspassi.laskennan-tulokset.vaihe') +
              '' +
              vaihe['vaihe-nro'] +
              ' (' +
              EitherMaybe.orSome(
                'ei aloitusvuotta',
                vaihe.tulokset['vaiheen-alku-pvm']
              ) +
              ') ' +
              $_('perusparannuspassi.laskennan-tulokset.kwh-per-vuosi')}</th>
        {/each}
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each laskennallisetOstoenergiat as { ppp_energiamuoto, et_energiamuoto }}
        <tr class="et-table--tr">
          <td class="et-table--td"
            >{$_(
              'perusparannuspassi.laskennan-tulokset.laskennallinen-ostoenergia.table-headers.' +
                ppp_energiamuoto
            )}</td>

          <td class="et-table--td"
            >{EitherMaybe.orSome(
              0,
              energiatodistus.tulokset['kaytettavat-energiamuodot'][
                et_energiamuoto
              ]
            )}</td>

          {#each perusparannuspassi.vaiheet as vaihe}
            <td class="et-table--td">
              <Input
                {schema}
                center={false}
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
                  ppp_energiamuoto
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

  <H4
    text={$_(
      'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.header'
    )} />
  <p>
    {$_('perusparannuspassi.laskennan-tulokset.info-before-table')}
  </p>
  <table class="et-table">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned">
          {$_(
            'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.energialaji'
          )}
        </th>
        <th class="et-table--th et-table--th-right-aligned">
          {$_('perusparannuspassi.laskennan-tulokset.lahtotilanne')}
        </th>

        {#each perusparannuspassi.vaiheet as vaihe}
          <th class="et-table--th et-table--th-right-aligned">
            {$_('perusparannuspassi.laskennan-tulokset.vaihe') +
              '' +
              vaihe['vaihe-nro'] +
              ' (' +
              EitherMaybe.orSome(
                'ei aloitusvuotta',
                vaihe.tulokset['vaiheen-alku-pvm']
              ) +
              ') ' +
              $_('perusparannuspassi.laskennan-tulokset.kwh-per-vuosi')}
          </th>
        {/each}
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.uusiutuvan-energian-kokonaistuotto'
          )}
        </td>
        <td class="et-table--td">
          {R.sum(
            R.map(
              EitherMaybe.orSome(0),
              R.values(energiatodistus.tulokset['uusiutuvat-omavaraisenergiat'])
            )
          )}
        </td>

        {#each perusparannuspassi.vaiheet as vaihe}
          <td class="et-table--td">
            <Input
              {schema}
              center={false}
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
                'uusiutuvan-energian-kokonaistuotto'
              ]} />
          </td>
        {/each}
      </tr>
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.uusiutuvan-energian-hyodynnetty-osuus'
          )}
        </td>
        <td class="et-table--td"> TODO </td>
        {#each perusparannuspassi.vaiheet as vaihe}
          <td class="et-table--td">
            <Input
              {schema}
              center={false}
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
                'uusiutuvan-energian-hyodynnetty-osuus'
              ]} />
          </td>
        {/each}
      </tr>
    </tbody>
  </table>

  <H4
    text={$_(
      'perusparannuspassi.laskennan-tulokset.toteutunut-ostoenergia.header'
    )} />
  <p>
    {$_('perusparannuspassi.laskennan-tulokset.info-before-table')}
  </p>

  <table class="et-table">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th-left-aligned">
          {$_(
            'perusparannuspassi.laskennan-tulokset.toteutunut-ostoenergia.header'
          )}
        </th>
        <th class="et-table--th et-table--th-right-aligned">
          {$_('perusparannuspassi.laskennan-tulokset.lahtotilanne')}
        </th>
        {#each perusparannuspassi.vaiheet as vaihe}
          <th class="et-table--th et-table--th-right-aligned">
            {$_('perusparannuspassi.laskennan-tulokset.vaihe') +
              '' +
              vaihe['vaihe-nro'] +
              ' (' +
              EitherMaybe.orSome(
                'ei aloitusvuotta',
                vaihe.tulokset['vaiheen-alku-pvm']
              ) +
              ') ' +
              $_('perusparannuspassi.laskennan-tulokset.kwh-per-vuosi')}
          </th>
        {/each}
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each toteutuneetOstoenergiat as { ppp_energiamuoto, et_energiamuoto }}
        <tr class="et-table--tr">
          <td class="et-table--td et-table--th-left-aligned">
            {$_(
              'perusparannuspassi.laskennan-tulokset.toteutunut-ostoenergia.table-headers.' +
                ppp_energiamuoto
            )}
          </td>
          <td class="et-table--td">
            {EitherMaybe.orSome('', et_energiamuoto(energiatodistus))}
          </td>

          {#each perusparannuspassi.vaiheet as vaihe}
            <td class="et-table--td">
              <Input
                {schema}
                center={false}
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
                  ppp_energiamuoto
                ]} />
            </td>
          {/each}
        </tr>
      {/each}
    </tbody>
  </table>
</div>
