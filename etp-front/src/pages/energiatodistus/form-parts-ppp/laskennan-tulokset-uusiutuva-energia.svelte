<script>
  import * as R from 'ramda';

  import * as EitherMaybe from '@Utility/either-maybe.js';
  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';

  import H4 from '@Component/H/H4';
  import { _ } from '@Language/i18n';
  import Input from '@Pages/energiatodistus/Input';
  import * as PPPUtils from './ppp-utils.js';
  import KwheM2 from '../form-parts/units/annual-kwhe-m2.svelte';

  export let energiatodistus;
  export let perusparannuspassi;
  export let schema;
  export let disabled = false;

  $: lahtotilanneOsuus = ETUtils.uusiutuvanEnergianOsuus(energiatodistus);
</script>

<H4
  text={$_('perusparannuspassi.laskennan-tulokset.uusiutuva-energia.header')} />
<p>
  {$_('perusparannuspassi.laskennan-tulokset.info-kirjaa-arvot')}
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
        {$_('perusparannuspassi.laskennan-tulokset.lahtotilanne-kwh-vuosi')}
      </th>

      {#each perusparannuspassi.vaiheet as vaihe}
        <th class="et-table--th et-table--th-right-aligned">
          {PPPUtils.formatVaiheHeading(
            `${$_('perusparannuspassi.laskennan-tulokset.vaihe')} ${vaihe['vaihe-nro']}`,
            '',
            vaihe.tulokset['vaiheen-alku-pvm'],
            $_('perusparannuspassi.laskennan-tulokset.ei-aloitusvuotta')
          )}
        </th>
      {/each}
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    <tr class="et-table--tr">
      <td class="et-table--td">
        {$_(
          'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.vuotuinen-tuotto'
        )}
        <span class="block">
          <KwheM2 />
        </span>
      </td>
      <td class="et-table--td">
        {R.sum(
          R.map(
            EitherMaybe.orSome(0),
            R.compose(
              R.values,
              R.pick([
                'aurinkosahko',
                'tuulisahko',
                'aurinkolampo',
                'muulampo',
                'muusahko',
                'lampopumppu'
              ])
            )(
              energiatodistus.tulokset[
                'uusiutuvat-omavaraisenergiat-kokonaistuotanto'
              ]
            )
          )
        )}
      </td>

      {#each perusparannuspassi.vaiheet as vaihe}
        <td class="et-table--td">
          <Input
            {schema}
            center={true}
            bind:model={perusparannuspassi}
            compact={true}
            i18nRoot="perusparannuspassi"
            disabled={disabled || PPPUtils.isVaiheInactive(vaihe)}
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
          'perusparannuspassi.laskennan-tulokset.uusiutuva-energia.hyodynnetty-osuus'
        )}
      </td>
      <td class="et-table--td">
        {lahtotilanneOsuus != null ? `${lahtotilanneOsuus}` : ''}
      </td>
      {#each perusparannuspassi.vaiheet as vaihe}
        <td class="et-table--td">
          <Input
            {schema}
            center={true}
            bind:model={perusparannuspassi}
            compact={true}
            i18nRoot="perusparannuspassi"
            disabled={disabled || PPPUtils.isVaiheInactive(vaihe)}
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
