<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as fxmath from '@Utility/fxmath';

  import { _ } from '@Language/i18n';
  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';

  import Area from '@Component/Energiatodistus/form-parts/units/area';
  import CubicMetre from '@Component/Energiatodistus/form-parts/units/cubic-metre';
  import LampokapasiteettiUnit from '@Component/Energiatodistus/form-parts/units/lampokapasiteetti';
  import IlmanvuotolukuUnit from '@Component/Energiatodistus/form-parts/units/ilmanvuotoluku';
  import Q50 from '@Component/Energiatodistus/form-parts/units/q50';
  import Crak from '@Component/Energiatodistus/form-parts/units/c-rak';
  import V from '@Component/Energiatodistus/form-parts/units/v';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import * as formats from '@Utility/formats';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: UA = EtUtils.rakennusvaippaUA(energiatodistus);

  $: osuudetLampohavioista = R.map(
    EtUtils.partOfSum(EtUtils.sumEtValues(UA)),
    UA
  );
</script>

<H3 text={$_('energiatodistus.lahtotiedot.rakennusvaippa.header')} />

<div class="flex lg:flex-row flex-col">
  <div class="w-1/3 py-4 mb-6 flex flex-row items-end">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'rakennusvaippa', 'ilmanvuotoluku']}
      unit={IlmanvuotolukuUnit}
      labelUnit={Q50} />
  </div>

  <div class="w-1/3 py-4 mb-6 flex flex-row items-end">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'rakennusvaippa', 'lampokapasiteetti']}
      unit={LampokapasiteettiUnit}
      labelUnit={Crak} />
  </div>

  <div class="w-1/3 py-4 mb-6 flex flex-row items-end">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'rakennusvaippa', 'ilmatilavuus']}
      unit={CubicMetre}
      labelUnit={V} />
  </div>
</div>

<div class="min-w-full overflow-x-auto">
  <table class="et-table mb-6">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th" />
        <th class="et-table--th">
          <span>{$_('energiatodistus.lahtotiedot.ala')}</span>
          <span class="block">
            <Area />
          </span>
        </th>
        <th class="et-table--th">
          <span>{$_('energiatodistus.lahtotiedot.U')}</span>
          <span class="block">W/(m²K)</span>
        </th>
        <th class="et-table--th">
          <span>{$_('energiatodistus.lahtotiedot.rakennusvaippa.U*A')}</span>
          <span class="block">W/K</span>
        </th>
        <th class="et-table--th">
          <span>
            {$_('energiatodistus.lahtotiedot.rakennusvaippa.osuuslampohaviosta')}
          </span>
          <span class="block">%</span>
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each ['ulkoseinat', 'ylapohja', 'alapohja', 'ikkunat', 'ulkoovet'] as vaippa}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(`energiatodistus.lahtotiedot.rakennusvaippa.labels.${vaippa}`)}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'rakennusvaippa', vaippa, 'ala']} />
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'rakennusvaippa', vaippa, 'U']} />
          </td>
          <td class="et-table--td">
            {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(1) )), R.prop(vaippa) )(UA)}
          </td>
          <td class="et-table--td">
            {R.compose( Maybe.orSome(''), R.map(formats.percentFormat), R.prop(vaippa) )(osuudetLampohavioista)}
          </td>
        </tr>
      {/each}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_('energiatodistus.lahtotiedot.rakennusvaippa.kylmasillat')}
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'rakennusvaippa', 'kylmasillat-UA']} />
        </td>
        <td class="et-table--td">
          {R.compose( Maybe.orSome(''), R.map(formats.percentFormat), R.prop('kylmasillat-UA') )(osuudetLampohavioista)}
        </td>
      </tr>
    </tbody>
  </table>
</div>
