<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as Validation from '@Component/Energiatodistus/validation';
  import * as ET from '@Component/Energiatodistus/energiatodistus-utils';

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

  export let inputLanguage;

  const lammitysLens = R.lensPath(['lahtotiedot', 'lammitys']);
  const lammitysmuoto1Lens = R.compose(
    lammitysLens,
    R.lensProp('lammitysmuoto-1')
  );

  const lammitysmuoto2Lens = R.compose(
    lammitysLens,
    R.lensProp('lammitysmuoto-2')
  );

  const lammonjakoLens = R.compose(lammitysLens, R.lensProp('lammonjako'));
</script>

<style>
  .et-table--thead .et-table--th, 
  .et-table--tbody .et-table--td:first-child{
    text-overflow: ellipsis;
    @apply overflow-hidden px-1 text-sm;
  }
</style>

<H3 text={$_('energiatodistus.lahtotiedot.lammitys.header')} />

<div class="w-full py-4 mb-4">
  <Select
    id={'lahtotiedot.lammitys.lammitysmuoto-1.id'}
    items={R.map(R.prop('id'), lammitysmuoto)}
    format={ET.selectFormat(LocaleUtils.label($locale), lammitysmuoto)}
    parse={Maybe.Some}
    required={true}
    allowNone={false}
    bind:model={energiatodistus}
    lens={R.compose(lammitysmuoto1Lens, R.lensProp('id'))}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammitysmuoto-1.id')}
    {disabled} />
</div>

{#if Validation.isLammitysmuoto1KuvausRequired(energiatodistus)}
  <div transition:slide|local={{ duration: 200 }} class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['lahtotiedot', 'lammitys', 'lammitysmuoto-1', 'kuvaus']} />
  </div>
{/if}

<div class="w-full py-4 mb-4">
  <Select
    id={'lahtotiedot.lammitys.lammitysmuoto-2.id'}
    items={R.map(R.prop('id'), lammitysmuoto)}
    format={ET.selectFormat(LocaleUtils.label($locale), lammitysmuoto)}
    parse={Maybe.Some}
    allowNone={true}
    bind:model={energiatodistus}
    lens={R.compose(lammitysmuoto2Lens, R.lensProp('id'))}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammitysmuoto-2.id')}
    {disabled} />
</div>

{#if Validation.isLammitysmuoto2KuvausRequired(energiatodistus)}
  <div transition:slide|local={{ duration: 200 }} class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['lahtotiedot', 'lammitys', 'lammitysmuoto-2', 'kuvaus']} />
  </div>
{/if}

<div class="w-full py-4 mb-4">
  <Select
    id={'lahtotiedot.lammitys.lammonjako.id'}
    items={R.map(R.prop('id'), lammonjako)}
    format={ET.selectFormat(LocaleUtils.label($locale), lammonjako)}
    parse={Maybe.Some}
    allowNone={true}
    required={true}
    bind:model={energiatodistus}
    lens={R.compose(lammonjakoLens, R.lensProp('id'))}
    label={$_('energiatodistus.lahtotiedot.lammitys.lammonjako.id')}
    {disabled} />
</div>

{#if Validation.isLammonjakoKuvausRequired(energiatodistus)}
  <div transition:slide|local={{ duration: 200 }} class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['lahtotiedot', 'lammitys', 'lammonjako', 'kuvaus']} />
  </div>
{/if}

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th
        class="et-table--th"
        title={$_('energiatodistus.lahtotiedot.lammitys.tuotonhyotysuhde')}>
        {$_('energiatodistus.lahtotiedot.lammitys.tuotonhyotysuhde')}
      </th>
      <th
        class="et-table--th"
        title={$_(
          'energiatodistus.lahtotiedot.lammitys.jaonjaluovutuksenhyotysuhde'
        )}>
        {$_('energiatodistus.lahtotiedot.lammitys.jaonjaluovutuksenhyotysuhde')}
      </th>
      <th
        class="et-table--th"
        title={$_('energiatodistus.lahtotiedot.lammitys.lampokerroin')}>
        {$_('energiatodistus.lahtotiedot.lammitys.lampokerroin')}
      </th>
      <th
        class="et-table--th"
        title={$_(
          'energiatodistus.lahtotiedot.lammitys.apulaitteidensahkonkaytto'
        )}>
        {$_('energiatodistus.lahtotiedot.lammitys.apulaitteidensahkonkaytto')}
        <span class="block">
          <VuosikulutusPerAlaUnit />
        </span>
      </th>
      <th
        class="et-table--th"
        title={$_(
          'energiatodistus.lahtotiedot.lammitys.lampopumppu-tuotto-osuus'
        )}>
        {$_('energiatodistus.lahtotiedot.lammitys.lampopumppu-tuotto-osuus')}
      </th>
      <th
        class="et-table--th"
        title={$_(
          'energiatodistus.lahtotiedot.lammitys.lampohavio-lammittamaton-tila'
        )}>
        {$_(
          'energiatodistus.lahtotiedot.lammitys.lampohavio-lammittamaton-tila'
        )}
        <VuosikulutusUnit />
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    {#each ['tilat-ja-iv', 'lammin-kayttovesi'] as hyotysuhde}
      <tr class="et-table--tr">
        <td class="et-table--td" title={$_(`energiatodistus.lahtotiedot.lammitys.${hyotysuhde}.label`)}>
          {$_(`energiatodistus.lahtotiedot.lammitys.${hyotysuhde}.label`)}
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'lahtotiedot',
              'lammitys',
              hyotysuhde,
              'tuoton-hyotysuhde'
            ]} />
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
        <td class="et-table--td">
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
            path={[
              'lahtotiedot',
              'lammitys',
              hyotysuhde,
              'lampopumppu-tuotto-osuus'
            ]} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={[
              'lahtotiedot',
              'lammitys',
              hyotysuhde,
              'lampohavio-lammittamaton-tila'
            ]} />
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
    {#each ['takka', 'ilmalampopumppu'] as maaratuotto}
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
