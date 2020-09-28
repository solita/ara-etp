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

  const muunnoskertoimet = {
    'kevyt-polttooljy': Maybe.Some(10),
    'pilkkeet-havu-sekapuu': Maybe.Some(1300),
    'pilkkeet-koivu': Maybe.Some(1700),
    puupelletit: Maybe.Some(4.7)
  };

  $: muunnoskerrotutPolttoaineet = R.compose(
    R.map(R.apply(EtUtils.multiplyWithKerroin)),
    R.mergeWith(Array.of, muunnoskertoimet),
    EtUtils.polttoaineet
  )(energiatodistus);

  $: muunnoskerrotutPolttoaineetPerLammitettyNettoala = EtUtils.perLammitettyNettoala(
    energiatodistus,
    muunnoskerrotutPolttoaineet
  );

  $: muunnoskerrotutVapaatPolttoaineet = R.compose(
    R.map(R.apply(EtUtils.multiplyWithKerroin)),
    R.converge(R.zip, [EtUtils.vapaatKertoimet, EtUtils.vapaatPolttoaineet])
  )(energiatodistus);

  $: muunnoskerrotutVapaatPolttoaineetPerLammitettyNettoala = EtUtils.perLammitettyNettoala(
    energiatodistus,
    muunnoskerrotutVapaatPolttoaineet
  );
</script>

<H3
  compact={true}
  text={$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.header')} />

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th et-table--th__sixth" />
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.table-header.maara')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.table-header.yksikko')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        {$_('energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.table-header.muunnoskerroin')}
      </th>
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusUnit />
      </th>
      <th class="et-table--th et-table--th__sixth">
        <VuosikulutusPerAlaUnit />
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['kevyt-polttooljy', 'pilkkeet-havu-sekapuu', 'pilkkeet-koivu', 'puupelletit'] as polttoaine}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.${polttoaine}`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', polttoaine]} />
        </td>
        <td class="et-table--td">
          {$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.${polttoaine}-yksikko`)}
        </td>
        <td class="et-table--td">
          {R.compose( formats.numberFormat, Maybe.get, R.prop(polttoaine) )(muunnoskertoimet)}
        </td>
        <td class="et-table--td" title={$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.${polttoaine}-vuosikulutus`)}>
          {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(0) )), R.prop(polttoaine) )(muunnoskerrotutPolttoaineet)}
        </td>
        <td class="et-table--td" title={$_(`energiatodistus.toteutunut-ostoenergiankulutus.ostetut-polttoaineet.${polttoaine}-neliovuosikulutus`)}>
          {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(0) )), R.prop(polttoaine) )(muunnoskerrotutPolttoaineetPerLammitettyNettoala)}
        </td>
      </tr>
    {/each}
    {#each R.path(['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu'], energiatodistus) as muu, index}
      <tr class="et-table--tr">
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu', index, 'nimi']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu', index, 'maara-vuodessa']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu', index, 'yksikko']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['toteutunut-ostoenergiankulutus', 'ostetut-polttoaineet', 'muu', index, 'muunnoskerroin']} />
        </td>
        <td class="et-table--td">
          {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(0) )), R.nth(index) )(muunnoskerrotutVapaatPolttoaineet)}
        </td>
        <td class="et-table--td">
          {R.compose( Maybe.orSome(''), R.map(R.compose( formats.numberFormat, fxmath.round(0) )), R.nth(index) )(muunnoskerrotutVapaatPolttoaineetPerLammitettyNettoala)}
        </td>
      </tr>
    {/each}
  </tbody>
</table>
