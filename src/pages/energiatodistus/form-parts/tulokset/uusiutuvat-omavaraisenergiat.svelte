<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import VuosituottoUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';
  import VuosituottoAreaUnit from '@Pages/energiatodistus/form-parts/units/annual-energy-over-area.svelte';

  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: omavaraisenergiatPerLammitettyNettoala = R.compose(
    EtUtils.perLammitettyNettoala(energiatodistus),
    EtUtils.omavaraisenergiat
  )(energiatodistus);
</script>

<H3
  compact={true}
  text={$_(
    'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.header.2018'
  )} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__twocells" />
      <th class="et-table--th">
        <VuosituottoUnit />
      </th>
      <th class="et-table--th">
        <VuosituottoAreaUnit />
      </th>
      <th class="et-table--th" />
    </tr>
  </thead>

  <tbody class="et-table--tbody">
    {#each ['aurinkosahko', 'aurinkolampo', 'tuulisahko', 'lampopumppu', 'muusahko', 'muulampo'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(
            `energiatodistus.tulokset.uusiutuvat-omavaraisenergiat.labels.${energiamuoto}`
          )}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'uusiutuvat-omavaraisenergiat', energiamuoto]} />
        </td>
        <td class="et-table--td">
          {R.compose(
            Maybe.orSome(''),
            R.map(R.compose(formats.numberFormat, fxmath.round(0))),
            R.prop(energiamuoto)
          )(omavaraisenergiatPerLammitettyNettoala)}
        </td>
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
