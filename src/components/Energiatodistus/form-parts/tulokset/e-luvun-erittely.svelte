<script>
  import * as R from 'ramda';
  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import ELukuUnit from '@Component/Energiatodistus/form-parts/units/e-luku';
  import EVuosiKulutusUnit from '@Component/Energiatodistus/form-parts/units/e-painotettu-vuosikulutus.svelte';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';

  export let disabled;
  export let schema;
  export let energiatodistus;

  $: energiamuotokertoimet = EtUtils.energiamuotokertoimet2018();

  $: ostoenergiat = EtUtils.ostoenergiat(energiatodistus);

  $: ostoenergiaSum = EtUtils.sumEtValues(ostoenergiat);

  $: painotetutOstoenergiankulutukset = R.compose(
    R.map(R.apply(EtUtils.multiplyWithKerroin)),
    R.mergeWith(Array.of)
  )(energiamuotokertoimet, ostoenergiat);

  $: painotetutOstoenergiankulutuksetSum = R.compose(
    EtUtils.sumEtValues,
    R.map(R.map(Math.ceil))
  )(painotetutOstoenergiankulutukset);

  $: painotetutOstoenergiankulutuksetPerNelio = EtUtils.perLammitettyNettoala(
    energiatodistus,
    painotetutOstoenergiankulutukset
  );
</script>

<H3
  compact={true}
  text={$_('energiatodistus.tulokset.kaytettavat-energiamuodot.header')} />

<table class="et-table et-table__noborder mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.kaytettavat-energiamuodot')}
      </th>
      <th class="et-table--th">
        <span>{$_('energiatodistus.tulokset.kaytettavat-energiamuodot.vakioidun_kayton_ostoenergia')}</span>
        <span class="block"><VuosikulutusUnit/></span>
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.energiamuodon_kerroin')}
      </th>
      <th class="et-table--th et-table--th__twocells" colspan="2">
        <div>
          {$_('energiatodistus.tulokset.kaytettavat-energiamuodot.kertoimella_painotettu_energiankulutus')}
        </div>
        <div class="flex flex-row justify-around">
          <span><EVuosiKulutusUnit/></span>
          <span><ELukuUnit/></span>
        </div>
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kaukolampo', 'sahko', 'uusiutuva-polttoaine', 'fossiilinen-polttoaine', 'kaukojaahdytys'] as energiamuoto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.tulokset.kaytettavat-energiamuodot.labels.${energiamuoto}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['tulokset', 'kaytettavat-energiamuodot', energiamuoto]} />
        </td>
        <td class="et-table--td">
          {R.compose( Maybe.get, R.prop(energiamuoto) )(energiamuotokertoimet)}
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose( Maybe.orSome(''), R.map(Math.ceil), R.prop(energiamuoto) )(painotetutOstoenergiankulutukset)}
        </td>
        <td class="et-table--td et-table--td__fifth">
          {R.compose( Maybe.orSome(''), R.map(Math.ceil), R.prop(energiamuoto) )(painotetutOstoenergiankulutuksetPerNelio)}
        </td>
      </tr>
    {/each}
    <tr class="et-table--tr border-t-1 border-disabled">
      <td class="et-table--td uppercase">
        {$_('energiatodistus.tulokset.yhteensa')}
      </td>
      <td class="et-table--td">{Maybe.get(ostoenergiaSum)}</td>
      <td class="et-table--td" />
      <td class="et-table--td et-table--td__fifth">
        {Maybe.get(painotetutOstoenergiankulutuksetSum)}
      </td>
      <td class="et-table--td et-table--td__fifth" />
    </tr>
  </tbody>
</table>
