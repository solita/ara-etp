<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Pages/energiatodistus/form-parts/units/annual-energy-over-area';
  import VuosikulutusUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: kuormatPerLammitettyNettoala = R.compose(
    EtUtils.perLammitettyNettoala(energiatodistus),
    EtUtils.kuormat
  )(energiatodistus);
</script>

<H3 text={$_('energiatodistus.tulokset.lampokuormat.header')} />

<div class="min-w-full overflow-x-auto">
  <table class="et-table">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th__twocells et-table--th-left-aligned">
          {$_(`energiatodistus.tulokset.lampokuormat.table-header`)}
        </th>
        <th
          class="et-table--th et-table--th__twocells et-table--th-right-aligned">
          <VuosikulutusUnit />
        </th>
        <th
          class="et-table--th et-table--th__twocells et-table--th-right-aligned">
          <VuosikulutusPerAlaUnit />
        </th>
      </tr>
    </thead>

    <tbody class="et-table--tbody">
      {#each ['aurinko', 'ihmiset', 'kuluttajalaitteet', 'valaistus', 'kvesi'] as kuorma}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(`energiatodistus.tulokset.lampokuormat.labels.${kuorma}`)}
          </td>
          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['tulokset', 'lampokuormat', kuorma]} />
          </td>
          <td class="et-table--td">
            {R.compose(
              Maybe.orSome(''),
              R.map(R.compose(formats.numberFormat, fxmath.round(0))),
              R.prop(kuorma)
            )(kuormatPerLammitettyNettoala)}
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
</div>
