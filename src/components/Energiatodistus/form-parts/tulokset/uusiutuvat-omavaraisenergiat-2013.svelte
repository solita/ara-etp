<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as fxmath from '@Utility/fxmath';
  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import VuosituottoUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';
  import VuosituottoAreaUnit from '@Component/Energiatodistus/form-parts/units/annual-energy-over-area.svelte';

  export let disabled;
  export let schema;
  export let energiatodistus;


  const energiaPerLammitettyNettoala =
    R.compose(
      R.map(fxmath.round(1)),
      R.useWith(R.lift(R.divide),
        [index => EtUtils.energiatodistusPath(['tulokset', 'uusiutuvat-omavaraisenergiat', index]),
         EtUtils.energiatodistusPath(['lahtotiedot', 'lammitetty-nettoala'])]));

  $: console.log(energiatodistus.tulokset['uusiutuvat-omavaraisenergiat']);
</script>

<H3
  compact={true}
  text={$_('energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.header.2013')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__twocells" />
      <th class="et-table--th"><VuosituottoUnit/></th>
      <th class="et-table--th"><VuosituottoAreaUnit/></th>
      <th class="et-table--th" />
    </tr>
  </thead>

  <tbody class="et-table--tbody">
    {#each energiatodistus.tulokset['uusiutuvat-omavaraisenergiat'] as _, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['tulokset', 'uusiutuvat-omavaraisenergiat', index, 'nimi-fi']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'uusiutuvat-omavaraisenergiat', index, 'vuosikulutus']} />
        </td>
        <td class="et-table--td">
          {Maybe.orSome('', energiaPerLammitettyNettoala(index, energiatodistus))}
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
