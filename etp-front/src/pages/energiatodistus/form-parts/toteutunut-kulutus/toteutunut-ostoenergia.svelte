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
  import Textarea from '@Pages/energiatodistus/Textarea';

  export let disabled;
  export let schema;
  export let energiatodistus;
  export let inputLanguage;
  export let versio = 2018;

  $: ostoenergiat = EtUtils.toteutuneetOstoenergiat(energiatodistus);

  $: ostoenergiatSum = EtUtils.sumEtValues(ostoenergiat);

  $: toteutuneetOstoenergiatPerLammitettyNettoala =
    EtUtils.perLammitettyNettoala(energiatodistus, ostoenergiat);

  $: toteutuneetOstoenergiatPerLammitettyNettoalaSum = R.compose(
    EtUtils.sumEtValues
  )(toteutuneetOstoenergiatPerLammitettyNettoala);
</script>

<H3
  text={$_(
    'energiatodistus.toteutunut-ostoenergiankulutus.toteutuneet-yhteensa-header'
  )} />

{#if R.equals(versio, 2026)}
  <div class="w-full flex-col">
    <div class="w-1/3">
      <Input
        {disabled}
        {schema}
        bind:model={energiatodistus}
        path={['toteutunut-ostoenergiankulutus', 'tietojen-alkuperavuosi']} />
    </div>
    <div class="mb-4 w-full pt-4">
      <Textarea
        {disabled}
        {schema}
        inputLanguage={Maybe.Some(inputLanguage)}
        bind:model={energiatodistus}
        path={['toteutunut-ostoenergiankulutus', 'lisatietoja']} />
    </div>
  </div>
{/if}

<div class="min-w-full overflow-x-auto md:overflow-x-hidden">
  <table class="et-table et-table__noborder border-r-0">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th et-table--th__4-6 et-table--th-left-aligned"
          >{$_(
            `energiatodistus.toteutunut-ostoenergiankulutus.toteutuneet-yhteensa-table-header`
          )}</th>
        <th class="et-table--th et-table--th__sixth et-table--th-right-aligned">
          <VuosikulutusUnit />
        </th>
        <th class="et-table--th et-table--th__sixth et-table--th-right-aligned">
          <VuosikulutusPerAlaUnit />
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each ['sahko-vuosikulutus-yhteensa', 'kaukolampo-vuosikulutus-yhteensa', 'polttoaineet-vuosikulutus-yhteensa', 'kaukojaahdytys-vuosikulutus-yhteensa'] as energiamuoto}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              `energiatodistus.toteutunut-ostoenergiankulutus.labels.${energiamuoto}`
            )}
          </td>

          <td class="et-table--td">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['toteutunut-ostoenergiankulutus', energiamuoto]} />
          </td>
          <td class="et-table--td">
            {R.compose(
              Maybe.orSome(''),
              R.map(R.compose(formats.numberFormat, fxmath.round(0))),
              R.prop(energiamuoto)
            )(toteutuneetOstoenergiatPerLammitettyNettoala)}
          </td>
        </tr>
      {/each}
      <tr class="et-table--tr border-t-1 border-disabled">
        <td class="et-table--td uppercase">{$_('energiatodistus.yhteensa')}</td>
        <td class="et-table--td">
          {R.compose(
            formats.numberFormat,
            Maybe.get,
            R.map(fxmath.round(0))
          )(ostoenergiatSum)}
        </td>
        <td class="et-table--td">
          {R.compose(
            formats.numberFormat,
            fxmath.round(0),
            Maybe.get
          )(toteutuneetOstoenergiatPerLammitettyNettoalaSum)}
        </td>
      </tr>
    </tbody>
  </table>
</div>
