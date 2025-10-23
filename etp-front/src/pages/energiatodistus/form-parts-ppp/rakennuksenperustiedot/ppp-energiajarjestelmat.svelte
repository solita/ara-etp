<script>
  import * as R from 'ramda';
  import * as Maybe from '@/utils/maybe-utils.js';
  import { _ } from '@Language/i18n.js';
  import { locale } from '@Language/i18n';
  import H4 from '@Component/H/H4.svelte';
  import Textarea from '@Pages/energiatodistus/Textarea';
  import Select from '@Component/Select/Select.svelte';
  import * as LocaleUtils from '@Language/locale-utils.js';
  import * as et from '@Pages/energiatodistus/energiatodistus-utils';

  export let schema;
  export let perusparannuspassi;
  export let luokittelut;
  export let disabled;
  export let energiatodistus;
  export let inputLanguage;

  $: labelLocale = LocaleUtils.label($locale);

  $: energiajarjestelmatConfig = {
    paalammitysjarjestelma: {
      items: R.pluck('id', luokittelut.lammitysmuoto),
      format: et.selectFormat(labelLocale, luokittelut.lammitysmuoto),
      lens: R.lensPath([
        'rakennuksen-perustiedot',
        'paalammitysjarjestelma-ehdotettu-taso'
      ])
    },
    ilmanvaihto: {
      items: R.pluck('id', luokittelut.ilmanvaihtotyypit),
      format: et.selectFormat(labelLocale, luokittelut.ilmanvaihtotyypit),
      lens: R.lensPath([
        'rakennuksen-perustiedot',
        'ilmanvaihto-ehdotettu-taso'
      ])
    },
    uusiutuvaenergia: {
      items: R.pluck('id', luokittelut.uusiutuvaEnergia),
      format: et.selectFormat(labelLocale, luokittelut.uusiutuvaEnergia),
      lens: R.lensPath([
        'rakennuksen-perustiedot',
        'uusiutuva-energia-ehdotettu-taso'
      ])
    },
    jaahdytys: {
      items: R.pluck('id', luokittelut.jaahdytys),
      format: et.selectFormat(labelLocale, luokittelut.jaahdytys),
      lens: R.lensPath(['rakennuksen-perustiedot', 'jaahdytys-ehdotettu-taso'])
    },
    mahdollisuusliittya: {
      format: et.selectFormat(labelLocale, luokittelut.mahdollisuusLiittya)
    }
  };

  const currentValuePaths = {
    paalammitysjarjestelma: [
      'lahtotiedot',
      'lammitys',
      'lammitysmuoto-1',
      'id'
    ],
    ilmanvaihto: ['lahtotiedot', 'ilmanvaihto', 'tyyppi-id'],
    uusiutuvaenergia: [
      'rakennuksen-perustiedot',
      'uusiutuva-energia-ehdotettu-taso'
    ],
    jaahdytys: ['rakennuksen-perustiedot', 'jaahdytys-ehdotettu-taso']
  };
</script>

<div class="py-4">
  <H4
    text={$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.header'
    )} />
</div>

<div class="min-w-full overflow-x-visible md:overflow-x-visible border-right-0">
  <table class="et-table mb-12">
    <thead class="et-table--thead">
      <tr class="et-table--tr">
        <th
          class="et-table--th et-table--th__twocells et-table--th-left-aligned">
          {$_(
            `perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.title`
          )}
        </th>
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.nykyinen'
          )}
        </th>
        <th
          class="et-table--th et-table--th-left-aligned et-table--th__twocells">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.ehdotettu-taso'
          )}
        </th>
      </tr>
    </thead>
    <tbody class="et-table--tbody">
      {#each ['paalammitysjarjestelma', 'ilmanvaihto', 'uusiutuvaenergia', 'jaahdytys'] as energiajarjestelma}
        <tr class="et-table--tr">
          <td class="et-table--td">
            {$_(
              `perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.labels.${energiajarjestelma}`
            )}
          </td>
          <td>
            {#if R.path(currentValuePaths[energiajarjestelma], energiatodistus)?.isValue}
              {energiajarjestelmatConfig[energiajarjestelma].format(
                R.path(currentValuePaths[energiajarjestelma], energiatodistus)
                  .val
              )}
            {:else}
              –
            {/if}
          </td>
          <td>
            <Select
              allowNone={false}
              bind:model={perusparannuspassi}
              lens={energiajarjestelmatConfig[energiajarjestelma].lens}
              items={energiajarjestelmatConfig[energiajarjestelma].items}
              format={energiajarjestelmatConfig[energiajarjestelma].format}
              parse={Maybe.Some} />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
  <div class="py-4">
    {$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.lisatiedot.info'
    )}
  </div>
  <div class="w-full py-4 mb-4">
    <Textarea
      {schema}
      i18nRoot="perusparannuspassi"
      bind:model={perusparannuspassi}
      path={['rakennuksen-perustiedot', 'lisatietoja']}
      inputLanguage={Maybe.Some(inputLanguage)} />
  </div>
  <div class="w-full py-4">
    <div class="py-4">
      <H4
        text={$_(
          'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.korjausrakentamisen-vahimmaisvaatimustaso.header'
        )} />
    </div>
    {$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.korjausrakentamisen-vahimmaisvaatimustaso.title'
    )}
  </div>
  <div class="w-full py-4">
    <div class="py-4">
      <H4
        text={$_(
          'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.kohteenliitettavyys.header'
        )} />
    </div>
    {$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.kohteenliitettavyys.info'
    )}
    <div class="lg:w-1/2 w-full py-4">
      <Select
        label={$_(
          'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.kohteenliitettavyys.title'
        )}
        {disabled}
        bind:model={perusparannuspassi}
        lens={R.lensPath([
          'rakennuksen-perustiedot',
          'mahdollisuus-liittya-energiatehokkaaseen'
        ])}
        items={R.pluck('id', luokittelut.mahdollisuusLiittya)}
        format={energiajarjestelmatConfig.mahdollisuusliittya.format}
        parse={Maybe.Some}
        required={true} />
    </div>
  </div>
</div>
