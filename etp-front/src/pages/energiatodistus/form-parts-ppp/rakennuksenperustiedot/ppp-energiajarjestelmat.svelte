<script>
  import * as R from 'ramda';
  import * as Maybe from '@/utils/maybe-utils.js';
  import { _ } from '@Language/i18n.js';
  import { locale } from '@Language/i18n';

  import H3 from '@Component/H/H3.svelte';
  import Input from '@Pages/energiatodistus/Input';
  import Select from '@Component/Select/Select2.svelte';
  import * as LocaleUtils from '@Language/locale-utils.js';

  export let schema;
  export let perusparannuspassi;
  export let mahdollisuusliittya;
  export let disabled;
  export let paalammitysjarjestelma;
  export let lammitysmuoto;
  export let energiatodistus;
  export let ilmanvaihto;
  export let uusiutuvaenergia;
  export let jaahdytysjarjestelma;

  $: labelLocale = LocaleUtils.label($locale);

  $: mahdollisuusliittyaIds = R.pluck('id', mahdollisuusliittya);

  $: paalammitysjarjestelma = R.pluck('id', lammitysmuoto);

  $: ilmanvaihtoIds = R.pluck('id', ilmanvaihto);

  $: uusiutuvaenergiaIds = R.pluck('id', uusiutuvaenergia);

  $: jaahdytysIds = R.pluck('id', jaahdytysjarjestelma);

  $: formatMahdollisuusliittya = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    Maybe.findById(R.__, mahdollisuusliittya)
  );

  $: formatLammitysmuoto = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    Maybe.findById(R.__, lammitysmuoto)
  );

  $: formatIlmanvaihto = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    Maybe.findById(R.__, ilmanvaihto)
  );

  $: formatUusiutuvaEnergia = R.compose(
      Maybe.orSome(''),
      R.map(labelLocale),
      Maybe.findById(R.__, uusiutuvaenergia)
  );

  $: formatJaahdytys = R.compose(
      Maybe.orSome(''),
      R.map(labelLocale),
      Maybe.findById(R.__, jaahdytysjarjestelma)
  );
  $: energiajarjestelmatConfig = {
    paalammitysjarjestelma: {
      model: energiatodistus,
      items: paalammitysjarjestelma,
      format: formatLammitysmuoto,
      lens: R.lensProp('lahtotiedot', 'lammitys', 'lammitysmuoto-1', 'id')
    },
    ilmanvaihto: {
      model: energiatodistus,
      items: ilmanvaihtoIds,
      format: formatIlmanvaihto,
      lens: R.lensPath(['lahtotiedot', 'ilmanvaihto'])
    },
    uusiutuvaenergia: {
      model: perusparannuspassi,
      items: uusiutuvaenergiaIds,
      format: formatUusiutuvaEnergia,
      lens: R.lensPath(['rakennuksen-perustiedot', 'uusiutuva-energia-ehdotettu-taso'])
    },
  
    jaahdytys: {
      model: perusparannuspassi,
      items: jaahdytysIds,
      format: formatJaahdytys,
      lens: R.lensPath(['rakennuksen-perustiedot', 'jaahdytys-ehdotettu-taso'])
    }
  };

  $: currentValuePaths = {
    paalammitysjarjestelma: [
      'lahtotiedot',
      'lammitys',
      'lammitysmuoto-1',
      'id'
    ],
    ilmanvaihto: ['lahtotiedot', 'ilmanvaihto', 'tyyppi-id'],
    uusiutuvaenergia: ['rakennuksen-perustiedot', 'uusiutuva-energia-ehdotettu-taso'],
    jaahdytys: ['rakennuksen-perustiedot', 'jaahdytys-ehdotettu-taso']
  };
</script>

<H3
  text={$_(
    'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.header'
  )} />

<div class="min-w-full overflow-x-auto md:overflow-x-hidden border-right-0">
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
          class="et-table--th et-table--th-right-aligned et-table--th__twocells">
          {$_(
            'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.nykyinen'
          )}
        </th>
        <th
          class="et-table--th et-table--th-right-aligned et-table--th__twocells">
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
              {disabled}
              model={energiajarjestelmatConfig[energiajarjestelma].model}
              lens={energiajarjestelmatConfig[energiajarjestelma].lens}
              items={energiajarjestelmatConfig[energiajarjestelma].items}
              format={energiajarjestelmatConfig[energiajarjestelma].format} />
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
  {$_(
    'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.lisatiedot.info'
  )}
  <div class="lg:w-3/5 w-full py-4">
    <Input
      {schema}
      i18nRoot="perusparannuspassi"
      bind:model={perusparannuspassi}
      path={['rakennuksen-perustiedot', 'lisatiedot']} />
  </div>
  <div class="w-full py-4">
    <H3
      text={$_(
        'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.korjausrakentamisen-vahimmaisvaatimustaso.header'
      )} />
    {$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.korjausrakentamisen-vahimmaisvaatimustaso.title'
    )}
  </div>
  <H3
    text={$_(
      'perusparannuspassi.rakennuksen-perustiedot.energiajarjestelmat.kohteenliitettavyys.header'
    )} />
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
      lens={R.lensProp('rakennuksen-perustiedot')}
      items={mahdollisuusliittyaIds}
      format={formatMahdollisuusliittya}
      required={true} />
  </div>
</div>
