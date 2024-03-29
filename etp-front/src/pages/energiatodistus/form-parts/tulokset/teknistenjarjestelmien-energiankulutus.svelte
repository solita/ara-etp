<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Pages/energiatodistus/form-parts/units/annual-energy-over-area';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: sahkoSum = R.compose(
    EtUtils.sumEtValues,
    EtUtils.teknistenJarjestelmienSahkot
  )(energiatodistus);

  $: lampoSum = R.compose(
    EtUtils.sumEtValues,
    EtUtils.teknistenJarjestelmienLammot
  )(energiatodistus);

  $: kaukojaahdytysSum = R.compose(
    EtUtils.sumEtValues,
    EtUtils.teknistenJarjestelmienKaukojaahdytys
  )(energiatodistus);
</script>

<H3
  compact={true}
  text={$_('energiatodistus.tulokset.tekniset-jarjestelmat.header')} />

<div class="min-w-full overflow-x-auto">
  <table class="et-table et-table__noborder mb-6">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th__twocells" />
        <th class="et-table--th">
          <span>
            {$_('energiatodistus.tulokset.tekniset-jarjestelmat.sahko')}
          </span>
          <span class="block">
            <VuosikulutusPerAlaUnit />
          </span>
        </th>
        <th class="et-table--th">
          <span>
            {$_('energiatodistus.tulokset.tekniset-jarjestelmat.lampo')}
          </span>
          <span class="block">
            <VuosikulutusPerAlaUnit />
          </span>
        </th>
        <th class="et-table--th">
          <span>
            {$_(
              'energiatodistus.tulokset.tekniset-jarjestelmat.kaukojaahdytys'
            )}
          </span>
          <span class="block">
            <VuosikulutusPerAlaUnit />
          </span>
        </th>
      </tr>
    </thead>

    <tbody class="et-table--tbody">
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            'energiatodistus.tulokset.tekniset-jarjestelmat.lammitysjarjestelma'
          )}
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
      {#each ['tilojen-lammitys', 'tuloilman-lammitys', 'kayttoveden-valmistus'] as jarjestelma}
        <tr class="et-table--tr">
          <td class="et-table--td">
            <span class="pl-8">
              {$_(
                `energiatodistus.tulokset.tekniset-jarjestelmat.labels.${jarjestelma}`
              )}
            </span>
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={[
                'tulokset',
                'tekniset-jarjestelmat',
                jarjestelma,
                'sahko'
              ]} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={[
                'tulokset',
                'tekniset-jarjestelmat',
                jarjestelma,
                'lampo'
              ]} />
          </td>
          <td class="et-table--td" />
        </tr>
      {/each}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_('energiatodistus.tulokset.tekniset-jarjestelmat.labels.iv-sahko')}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'tekniset-jarjestelmat', 'iv-sahko']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            'energiatodistus.tulokset.tekniset-jarjestelmat.labels.jaahdytys'
          )}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'tekniset-jarjestelmat',
              'jaahdytys',
              'sahko'
            ]} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'tekniset-jarjestelmat',
              'jaahdytys',
              'kaukojaahdytys'
            ]} />
        </td>
      </tr>
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            'energiatodistus.tulokset.tekniset-jarjestelmat.labels.kuluttajalaitteet-ja-valaistus-sahko'
          )}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'tulokset',
              'tekniset-jarjestelmat',
              'kuluttajalaitteet-ja-valaistus-sahko'
            ]} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td uppercase">
          {$_('energiatodistus.tulokset.yhteensa')}
        </td>
        <td class="et-table--td">
          {R.compose(
            formats.numberFormat,
            Maybe.get,
            R.map(fxmath.round(0))
          )(sahkoSum)}
        </td>
        <td class="et-table--td">
          {R.compose(
            formats.numberFormat,
            Maybe.get,
            R.map(fxmath.round(0))
          )(lampoSum)}
        </td>
        <td class="et-table--td">
          {R.compose(
            formats.numberFormat,
            Maybe.get,
            R.map(fxmath.round(0))
          )(kaukojaahdytysSum)}
        </td>
      </tr>
    </tbody>
  </table>
</div>
