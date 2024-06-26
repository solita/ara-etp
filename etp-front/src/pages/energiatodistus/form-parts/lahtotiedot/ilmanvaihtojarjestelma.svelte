<script>
  import { slide } from 'svelte/transition';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { _, locale } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as Validation from '@Pages/energiatodistus/validation';

  import H3 from '@Component/H/H3';
  import Input from '@Pages/energiatodistus/Input';
  import Select from '@Component/Select/Select';

  export let disabled;
  export let schema;
  export let energiatodistus;

  export let ilmanvaihtotyypit;
  export let inputLanguage;

  const tyyppiLens = R.lensPath(['lahtotiedot', 'ilmanvaihto', 'tyyppi-id']);
</script>

<H3 text={$_('energiatodistus.lahtotiedot.ilmanvaihto.header')} />

<div class="w-full py-4 mb-4">
  <Select
    id={'lahtotiedot.ilmanvaihto.tyyppi-id'}
    items={R.map(R.prop('id'), ilmanvaihtotyypit)}
    format={ET.selectFormat(LocaleUtils.label($locale), ilmanvaihtotyypit)}
    parse={Maybe.Some}
    required={true}
    validation={schema.$signature}
    allowNone={false}
    bind:model={energiatodistus}
    lens={tyyppiLens}
    label={$_('energiatodistus.lahtotiedot.ilmanvaihto.tyyppi-id')}
    {disabled} />
</div>

{#if Validation.isIlmanvaihtoKuvausRequired(energiatodistus)}
  <div
    transition:slide={{ duration: 200 }}
    on:introend={evt => {
      const input = evt.target.getElementsByTagName('input')[0];
      input.focus();
    }}
    class="w-full py-4 mb-4">
    <Input
      {disabled}
      {schema}
      center={false}
      bind:model={energiatodistus}
      inputLanguage={Maybe.Some(inputLanguage)}
      path={['lahtotiedot', 'ilmanvaihto', 'kuvaus']} />
  </div>
{/if}

<div class="min-w-full overflow-x-auto">
  <table class="et-table mb-6">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th class="et-table--th" />
        <th class="et-table--th">
          {$_('energiatodistus.lahtotiedot.ilmanvaihto.ilmavirta')}
          <br />
          {$_('energiatodistus.lahtotiedot.ilmanvaihto.tulo')} (m³/s) / {$_(
            'energiatodistus.lahtotiedot.ilmanvaihto.poisto'
          )} (m³/s)
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
        <td class="et-table--td">
          <div class="flex flex-row items-center">
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'tulo']} />
            <span class="block" class:pb-3={disabled}>/</span>
            <Input
              {disabled}
              {schema}
              compact={true}
              bind:model={energiatodistus}
              path={['lahtotiedot', 'ilmanvaihto', 'paaiv', 'poisto']} />
          </div>
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
            {$_(
              `energiatodistus.lahtotiedot.ilmanvaihto.labels.${ilmanvaihto}`
            )}
          </td>
          <td class="et-table--td">
            <div class="flex flex-row items-center">
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'tulo']} />
              <span class="block" class:pb-3={disabled}>/</span>
              <Input
                {disabled}
                {schema}
                compact={true}
                bind:model={energiatodistus}
                path={['lahtotiedot', 'ilmanvaihto', ilmanvaihto, 'poisto']} />
            </div>
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
</div>

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
