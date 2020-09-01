<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';

  import Select from '@Component/Select/Select';
  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import VuosikulutusPerAlaUnit from '@Component/Energiatodistus/form-parts/units/annual-energy-over-area';
  import VuosikulutusUnit from '@Component/Energiatodistus/form-parts/units/annual-energy';

  export let disabled;
  export let schema;
  export let energiatodistus;

  export let lammitysmuoto;
  export let lammonjako;

  const lammitysmuotoMuuId = 9;
  const lammonjakoMuuId = 12;

  const lammitysLens = R.lensPath(['lahtotiedot', 'lammitys']);
  const lammitysmuoto1IdLens = R.compose(
    lammitysLens,
    R.lensPath(['lammitysmuoto-1', 'id'])
  );

  const lammitysmuoto2IdLens = R.compose(
    lammitysLens,
    R.lensPath(['lammitysmuoto-2', 'id'])
  );

  const lammonjakoLens = R.compose(
    lammitysLens,
    R.lensPath(['lammonjako', 'id'])
  );
</script>

<H3 text={$_('energiatodistus.lahtotiedot.lammitys.header')} />

<div class="w-full py-4 mb-4">
  <Select
    items={R.map(R.prop('id'), lammitysmuoto)}
    format={R.compose( LocaleUtils.label($locale), R.find(R.__, lammitysmuoto), R.propEq('id') )}
    parse={Maybe.Some}
    allowNone={false}
    bind:model={energiatodistus}
    lens={lammitysmuoto1IdLens}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammitysmuoto-1.id')} />
</div>

{#if R.compose( Maybe.exists(R.equals(lammitysmuotoMuuId)), R.view(lammitysmuoto1IdLens) )(energiatodistus)}
  <div class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lammitys', 'lammitysmuoto-1', 'kuvaus-fi']} />
  </div>
{/if}

<div class="w-full py-4 mb-4">
  <Select
    items={R.map(R.prop('id'), lammitysmuoto)}
    format={R.compose( LocaleUtils.label($locale), R.find(R.__, lammitysmuoto), R.propEq('id') )}
    parse={Maybe.Some}
    allowNone={true}
    bind:model={energiatodistus}
    lens={lammitysmuoto2IdLens}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammitysmuoto-2.id')} />
</div>

{#if R.compose( Maybe.exists(R.equals(lammitysmuotoMuuId)), R.view(lammitysmuoto2IdLens) )(energiatodistus)}
  <div class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lammitys', 'lammitysmuoto-2', 'kuvaus-fi']} />
  </div>
{/if}

<div class="w-full py-4 mb-4">
  <Select
    items={R.map(R.prop('id'), lammonjako)}
    format={R.compose( LocaleUtils.label($locale), R.find(R.__, lammonjako), R.propEq('id') )}
    parse={Maybe.Some}
    allowNone={false}
    bind:model={energiatodistus}
    lens={lammonjakoLens}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammonjako.id')} />
</div>

{#if R.compose( Maybe.exists(R.equals(lammonjakoMuuId)), R.view(lammonjakoLens) )(energiatodistus)}
  <div class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'lammitys', 'lammonjako', 'kuvaus-fi']} />
  </div>
{/if}

<div class="w-full py-4 mb-4">
  <Input
    {disabled}
    {schema}
    center={false}
    bind:model={energiatodistus}
    path={['lahtotiedot', 'lammitys', 'kuvaus-fi']} />
</div>

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.lammitys.tuotonhyotysuhde')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.lammitys.jaonjaluovutuksenhyotysuhde')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.lammitys.lampokerroin')}
      </th>
      <th class="et-table--th border-r-4">
        <span>
          {$_('energiatodistus.lahtotiedot.lammitys.apulaitteidensahkonkaytto')}
        </span>
        <span class="block">
          <VuosikulutusPerAlaUnit />
        </span>
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.lammitys.lampopumppu-tuotto-osuus')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.lammitys.lampohavio-lammittamaton-tila')}
        <VuosikulutusUnit />
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['tilat-ja-iv', 'lammin-kayttovesi'] as hyotysuhde}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.lammitys.${hyotysuhde}.label`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'tuoton-hyotysuhde']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'jaon-hyotysuhde']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'lampokerroin']} />
        </td>
        <td class="et-table--td border-r-4">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'apulaitteet']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'lampopumppu-tuotto-osuus']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', hyotysuhde, 'lampohavio-lammittamaton-tila']} />
        </td>
      </tr>
    {/each}
  </tbody>
</table>

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        <span>{$_('energiatodistus.lahtotiedot.lammitys.maara')}</span>
        <span class="block">{$_('units.pieces')}</span>
      </th>
      <th class="et-table--th">
        <span>{$_('energiatodistus.lahtotiedot.lammitys.tuotto')}</span>
        <span class="block">kWh</span>
      </th>
      <th class="et-table--th" />
      <th class="et-table--th" />
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['takka', 'ilmanlampopumppu'] as maaratuotto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.lammitys.${maaratuotto}.label`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', maaratuotto, 'maara']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'lammitys', maaratuotto, 'tuotto']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>
