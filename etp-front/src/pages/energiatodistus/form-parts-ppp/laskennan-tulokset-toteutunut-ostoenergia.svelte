<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EitherMaybe from '@Utility/either-maybe.js';

  import H4 from '@Component/H/H4';
  import { _ } from '@Language/i18n';
  import Input from '@Pages/energiatodistus/Input';
  import * as PPPUtils from './ppp-utils.js';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;

  const toteutuneetOstoenergiat = [
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-kaukolampo',
      label_key: 'kaukolampo',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukolampo-vuosikulutus'
        ]
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-sahko',
      label_key: 'sahko',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kokonaissahko-vuosikulutus'
        ]
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-uusiutuvat-pat',
      label_key: 'uusiutuvat-polttoaineet',
      et_energiamuoto: _ => Either.Right(Maybe.None())
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-fossiiliset-pat',
      label_key: 'fossiiliset-polttoaineet',
      et_energiamuoto: _ => Either.Right(Maybe.None())
    },
    {
      ppp_energiamuoto: 'toteutunut-ostoenergia-kaukojaahdytys',
      label_key: 'kaukojaahdytys',
      et_energiamuoto: et =>
        et['toteutunut-ostoenergiankulutus']['ostettu-energia'][
          'kaukojaahdytys-vuosikulutus'
        ]
    }
  ];
</script>

<H4
  text={$_(
    'perusparannuspassi.laskennan-tulokset.toteutunut-ostoenergia.header'
  )} />
<p>
  {$_('perusparannuspassi.laskennan-tulokset.info-kirjaa-arvot')}
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
        {$_('perusparannuspassi.laskennan-tulokset.lahtotilanne-kwh-vuosi')}
      </th>
      {#each perusparannuspassi.vaiheet as vaihe}
        <th class="et-table--th et-table--th-right-aligned">
          {PPPUtils.formatVaiheHeading(
            `${$_('perusparannuspassi.laskennan-tulokset.vaihe')} ${vaihe['vaihe-nro']}`,
            $_('perusparannuspassi.laskennan-tulokset.kwh-per-vuosi'),
            vaihe.tulokset['vaiheen-alku-pvm'],
            $_('perusparannuspassi.laskennan-tulokset.ei-aloitusvuotta')
          )}
        </th>
      {/each}
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each toteutuneetOstoenergiat as { ppp_energiamuoto, label_key, et_energiamuoto }}
      <tr class="et-table--tr">
        <td class="et-table--td et-table--th-left-aligned">
          {$_(
            'perusparannuspassi.laskennan-tulokset.toteutunut-ostoenergia.' +
              label_key
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
