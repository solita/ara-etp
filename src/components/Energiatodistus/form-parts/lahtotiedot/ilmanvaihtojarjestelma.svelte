<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as et from '@Component/Energiatodistus/energiatodistus-utils';

  import H3 from '@Component/H/H3';
  import Input from '@Component/Energiatodistus/Input';
  import Select from '@Component/Select/Select';

  export let disabled;
  export let schema;
  export let energiatodistus;

  export let ilmanvaihtotyypit;
  export let inputLanguage;

  const ilmanvaihtoMuuId = 6;

  const tyyppiLens = R.lensPath(['lahtotiedot', 'ilmanvaihto', 'tyyppi-id']);
</script>

<H3 text={$_('energiatodistus.lahtotiedot.ilmanvaihto.header')} />

<div class="w-full py-4 mb-4">
  <Select
    id={'lahtotiedot.ilmanvaihto.tyyppi-id'}
    items={R.map(R.prop('id'), ilmanvaihtotyypit)}
    format={et.selectFormat(LocaleUtils.label($locale), ilmanvaihtotyypit)}
    parse={Maybe.Some}
    allowNone={false}
    bind:model={energiatodistus}
    lens={tyyppiLens}
    label={$_('energiatodistus.lahtotiedot.ilmanvaihto.tyyppi-id')} />
</div>

{#if R.compose( Maybe.exists(R.equals(ilmanvaihtoMuuId)), R.view(tyyppiLens) )(energiatodistus)}
  <div transition:slide|local={{ duration: 200 }} class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['lahtotiedot', 'ilmanvaihto', 'kuvaus']} />
  </div>
{/if}

<table class="et-table mb-6">
  <thead class="et-table--thead">
    <tr class="et-table--tr">
      <th class="et-table--th" />
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.ilmavirta')}
        <br />
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.tulo')}(m³/s) / {$_('energiatodistus.lahtotiedot.ilmanvaihto.poisto')}(m³/s)
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.sfp-luku')}
      </th>
      <th class="et-table--th">
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.lampotilasuhde')}
      </th>
      <th class="et-table--th">
        <span>
          {$_('energiatodistus.lahtotiedot.ilmanvaihto.jaatymisenesto')}
        </span>
        <span class="block">{$_('units.celsius')}</span>
      </th>
    </tr>
  </thead>
  <tbody class="et-table--tbody">
    <tr class="et-table--tr">
      <td class="et-table--td">
        {$_('energiatodistus.lahtotiedot.ilmanvaihto.labels.paaiv')}
      </td>
      <td class="et-table--td flex flex-row items-end">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'tulo']} />
        /
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'poisto']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'sfp']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'lampotilasuhde']} />
      </td>
      <td class="et-table--td">
        <Input
          {disabled}
          {schema}
          compact={true}
          bind:model={energiatodistus}
          path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'jaatymisenesto']} />
      </td>
    </tr>
    {#each ['erillispoistot', 'ivjarjestelma'] as ilmanvaihto}
      <tr class="et-table--tr">
        <td class="et-table--td">
          {$_(`energiatodistus.lahtotiedot.ilmanvaihto.labels.${ilmanvaihto}`)}
        </td>
        <td class="et-table--td flex flex-row items-end">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'tulo']} />
          /
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'poisto']} />
        </td>
        <td class="et-table--td">
          <Input
            {disabled}
            {schema}
            compact={true}
            bind:model={energiatodistus}
            path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'sfp']} />
        </td>
        <td class="et-table--td" />
        <td class="et-table--td" />
      </tr>
    {/each}
  </tbody>
</table>

<div class="flex lg:flex-row flex-col lg:items-end">
  <div class="w-1/2 py-4 mb-4 mr-8">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'ilmanvaihto', 'lto-vuosihyotysuhde']} />
  </div>

  <div class="w-1/2 py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      path={['lahtotiedot', 'ilmanvaihto', 'tuloilma-lampotila']} />
  </div>
</div>
