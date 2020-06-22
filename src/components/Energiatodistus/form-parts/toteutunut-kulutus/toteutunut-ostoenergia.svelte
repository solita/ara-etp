<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';
  import { _ } from '@Language/i18n';
  import * as formats from '@Utility/formats';
  import * as fxmath from '@Utility/fxmath';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Component/Energiatodistus/form-parts/units/annual-energy-over-area';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: ostoenergiat = EtUtils.toteutuneetOstoenergiat(energiatodistus);

  $: ostoenergiatSum = EtUtils.sumEtValues(ostoenergiat);

  $: toteutuneetOstoenergiatPerLammitettyNettoala = EtUtils.perLammitettyNettoala(
    energiatodistus,
    ostoenergiat
  );

  $: toteutuneetOstoenergiatPerLammitettyNettoalaSum = R.compose(
    EtUtils.sumEtValues
  )(toteutuneetOstoenergiatPerLammitettyNettoala);
</script>

<H3
  compact={true}
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.toteutuneet-yhteensa')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusUnit />
      </th>
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusPerAlaUnit />
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['sahko-vuosikulutus-yhteensa', 'kaukolampo-vuosikulutus-yhteensa', 'polttoaineet-vuosikulutus-yhteensa', 'kaukojaahdytys-vuosikulutus-yhteensa'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.${energiamuoto}`)}
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td" />
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', energiamuoto]} />
        </td>
        <td class="et-table--td">
          {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(0) )), R.prop(energiamuoto) )(toteutuneetOstoenergiatPerLammitettyNettoala)}
        </td>
      </tr>
    {/each}
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">{$_('energiatodistus.yhteensa')}</td>
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td" />
      <td class="et-table--td">
        {R.compose( formats.numberFormat, Maybe.get, R.map(fxmath.round(0)) )(ostoenergiatSum)}
      </td>
      <td class="et-table--td">
        {R.compose( formats.numberFormat, fxmath.round(0), Maybe.get )(toteutuneetOstoenergiatPerLammitettyNettoalaSum)}
      </td>
    </tr>
  </tbody>
</table>
