<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Component/Energiatodistus/form-parts/units/annual-energy-over-area';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: kuormatPerLammitettyNettoala = R.compose(
    EtUtils.perLammitettyNettoala(energiatodistus),
    EtUtils.kuormat
  )(energiatodistus);
</script>

<H3 compact={true} text={$_('energiatodistus.tulokset.lampokuormat.header')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__twocells" />
      <th class="et-table--th">
        <VuosikulutusUnit/>
      </th>
      <th class="et-table--th">
        <VuosikulutusPerAlaUnit/>
      </th>
      <th class="et-table--th" />
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
          {R.compose( Maybe.orSome(''), R.map(Math.ceil), R.prop(kuorma) )(kuormatPerLammitettyNettoala)}
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
