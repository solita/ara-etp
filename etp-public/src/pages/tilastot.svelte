<script>
  import InfoTooltip, { AnchorPosition } from '@Component/info-tooltip';
  import Container, { styles as containerStyles } from '@Component/container';
  import InfoBlock from '@Component/info-block';
  import Input from '@Component/input-search';
  import InputNumber from '@Component/input-number';
  import InputSelect from '@Component/input-select';
  import TilastotEntriesList from '@Component/tilastot-entries-list';
  import TilastotEtVersion from '@Component/tilastot-et-version-block';
  import Button, { styles as buttonStyles } from '@Component/button';
  import { onMount, tick } from 'svelte';
  import { navigate } from '@/router/router';
  import { _, locale, labelLocale } from '@Localization/localization';
  import Seo from '@Component/seo';
  import * as api from '@/api/tilastot-api';
  import * as EtApi from '@/api/energiatodistus-api';
  import Spinner from '@Component/spinner';
  import * as Formats from '@/utilities/formats';
  import * as Parsers from '@/utilities/parsers';
  import { announceAssertively } from '@/utilities/announce';

  let resultsElem;
  let vuosiminInput, vuosimaxInput, nettoalaminInput, nettoalamaxInput;

  export let keyword = '';
  export let kayttotarkoitus = '';
  export let vuosimin = '';
  export let vuosimax = '';
  export let nettoalamin = '';
  export let nettoalamax = '';

  announceAssertively($_('TILASTOT'));

  const format = new Intl.NumberFormat('fi-FI', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  }).format;

  let searchmodel = {
    keyword,
    kayttotarkoitus,
    vuosimin,
    vuosimax,
    nettoalamin,
    nettoalamax
  };
  let searchCommitted = false;
  let printing = false;
  let resultKeyword = '';
  let resultKayttotarkoitus = '';
  let resultVuosimin = '';
  let resultVuosimax = '';
  let resultNettoalamin = '';
  let resultNettoalamax = '';
  let tilastotSearchPromise = new Promise(() => {});
  let results;
  let lammitysmuodot;
  let ilmanvaihtotyypit;

  let chartData2018, chartData2013;

  let total2013 = 0;
  let total2018 = 0;

  const commitSearch = evt => {
    searchCommitted = true;
    tilastotSearchPromise = new Promise(() => {});

    const qs = [
      ...(searchmodel.keyword
        ? [['keyword', searchmodel.keyword].join('=')]
        : []),
      ...(searchmodel.kayttotarkoitus
        ? [['kayttotarkoitus', searchmodel.kayttotarkoitus].join('=')]
        : []),

      ...(searchmodel.vuosimin
        ? [['vuosimin', searchmodel.vuosimin].join('=')]
        : []),
      ...(searchmodel.vuosimax
        ? [['vuosimax', searchmodel.vuosimax].join('=')]
        : []),
      ...(searchmodel.nettoalamin
        ? [['nettoalamin', searchmodel.nettoalamin].join('=')]
        : []),
      ...(searchmodel.nettoalamax
        ? [['nettoalamax', searchmodel.nettoalamax].join('=')]
        : [])
    ].join('&');

    navigate(`/tilastot${qs ? '?' + qs : ''}`);
    tilastotSearchPromise = Promise.all([
      api.statistics(fetch, {
        keyword: searchmodel.keyword,
        'kayttotarkoitus-id': searchmodel.kayttotarkoitus,
        'valmistumisvuosi-min': searchmodel.vuosimin,
        'valmistumisvuosi-max': searchmodel.vuosimax,
        'lammitetty-nettoala-min': searchmodel.nettoalamin,
        'lammitetty-nettoala-max': searchmodel.nettoalamax
      }),
      EtApi.lammitysmuoto(fetch),
      EtApi.ilmanvaihtotyyppi(fetch)
    ]).then(
      ([
        statisticsPromise,
        lammitysmuodotPromise,
        ilmanvaihtotyypitPromise
      ]) => {
        results = statisticsPromise;
        lammitysmuodot = lammitysmuodotPromise;
        ilmanvaihtotyypit = ilmanvaihtotyypitPromise;

        resultKeyword = searchmodel.keyword;
        resultKayttotarkoitus = searchmodel.kayttotarkoitus;
        resultVuosimin = searchmodel.vuosimin;
        resultVuosimax = searchmodel.vuosimax;
        resultNettoalamin = searchmodel.nettoalamin;
        resultNettoalamax = searchmodel.nettoalamax;

        total2018 = 0;
        total2013 = 0;
        for (let key in results?.['counts']?.['2018']?.['e-luokka']) {
          total2018 += results?.['counts']?.['2018']?.['e-luokka'][key];
        }

        for (let key in results?.['counts']?.['2013']?.['e-luokka']) {
          total2013 += results?.['counts']?.['2013']?.['e-luokka'][key];
        }

        chartData2018 = [
          (results?.['counts']?.['2018']?.['e-luokka']?.A || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.B || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.C || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.D || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.E || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.F || 0) / total2018,
          (results?.['counts']?.['2018']?.['e-luokka']?.G || 0) / total2018
        ];
        chartData2013 = [
          (results?.['counts']?.['2013']?.['e-luokka']?.A || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.B || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.C || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.D || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.E || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.F || 0) / total2013,
          (results?.['counts']?.['2013']?.['e-luokka']?.G || 0) / total2013
        ];

        resultsElem?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    );
  };

  const resetForm = async () => {
    await tick();
    searchmodel = Object.keys(searchmodel).reduce(
      (acc, key) => ({ ...acc, [key]: '' }),
      {}
    );
  };

  onMount(() => {
    if (Object.values(searchmodel).some(item => item.length)) {
      commitSearch();
    }
  });

  const kayttotarkoituksetPromise = api.kayttotarkoitukset(fetch);

  const parseAndFormatPercent = (total, str) =>
    Formats.formatPercent(Parsers.parsePercent(total, str));

  const formatPercent = str =>
    Formats.formatPercent((parseFloat(str) * 100).toFixed(0));

  window.onbeforeprint = () => {
    printing = true;
  };
  window.onafterprint = () => {
    printing = false;
  };

  onMount(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });
  });
</script>

<style>
  * {
    color-adjust: exact;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .pbb-always {
    page-break-before: always;
    break-before: always;
  }
  h1,
  h2,
  span {
    break-inside: avoid;
    page-break-inside: avoid;
  }

  div.statistics :global(h1) {
    @apply text-green;
  }

  div.statistics :global(h2) {
    @apply text-ashblue normal-case;
  }

  .only-print {
    display: none;
  }

  @media print {
    .only-print {
      display: block;
    }

    .results > .flex {
      display: block;
    }
  }
</style>

<Seo
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('TILASTOT')}"
  descriptionFi={$locale == 'fi' ? $_('TILASTOT') : undefined}
  descriptionSv={$locale == 'sv' ? $_('TILASTOT') : undefined} />
<div>
  <div class="print:hidden">
    <Container {...containerStyles.beige}>
      <InfoBlock title={$_('TILASTOT_INFO_TITLE')}>
        {$_('TILASTOT_INFO_TEXT')}
      </InfoBlock>
    </Container>
  </div>

  <Container {...containerStyles.white}>
    <div class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto flex flex-col">
      <!-- SEARTCH FORM -->
      <div class="flex flex-col w-full print:hidden">
        <form
          on:change={async evt => {
            searchmodel = {
              ...searchmodel,
              [evt.target.name]: evt.target.value
            };
            await tick();
          }}
          on:submit|preventDefault={evt => {
            commitSearch(evt.target);
          }}
          on:reset={resetForm}>
          <!-- MAIN HAKU -->
          {#await kayttotarkoituksetPromise}
            <div class="w-full my-4 py-4 justify-center">
              <Spinner />
            </div>
          {:then kayttotarkoitukset}
            <div
              class="flex flex-col lg:flex-row w-full items-end justify-start">
              <div class="flex flex-col w-full lg:w-7/12">
                <aside class="font-normal text-xs italic">
                  {$_('TILASTOT_HAE_ASIDE')}
                </aside>
                <div class="flex">
                  <div class="w-full lg:w-11/12">
                    <Input
                      label={$_('TILASTOT_HAE_ALUEELLA')}
                      name="keyword"
                      value={searchmodel.keyword} />
                  </div>
                </div>
              </div>

              <aside
                class="w-full mt-4 lg:mt-0 lg:w-5/12 lg:pl-4 lg:border-l-8 border-ashblue text-ashblue italic text-sm">
                {$_('TILASTOT_ASIDE')}
              </aside>
            </div>
            <!-- TARKENNETTU HAKU -->
            <div class="w-full lg:w-5/6 flex flex-col my-4 py-4 space-y-2">
              <div
                class="tarkennettu-row w-full mx-auto center flex flex-col lg:flex-row items-center">
                <div class="w-full lg:w-1/2">
                  <InfoTooltip
                    tooltip={$_('TILASTOT_TYYPPI_KAYTTOTARKOITUS_TOOLTIP')}
                    title={$_('TILASTOT_TYYPPI_KAYTTOTARKOITUS')}>
                    <span
                      class="tarkennettu-label tracking-widest text-ashblue">
                      {$_('TILASTOT_TYYPPI_KAYTTOTARKOITUS')}
                    </span>
                  </InfoTooltip>
                </div>
                <div class="w-full lg:w-1/2">
                  <InputSelect
                    name={'kayttotarkoitus'}
                    options={[
                      '',
                      ...kayttotarkoitukset.map(item => `${item['id']}`)
                    ]}
                    format={id => {
                      if (id === '') return $_('KAIKKI');
                      return labelLocale(
                        $locale,
                        kayttotarkoitukset.find(
                          item => item.id === parseInt(id, 10)
                        )
                      );
                    }}
                    label={$_('TILASTOT_TYYPPI_KAYTTOTARKOITUS')}
                    value={searchmodel.kayttotarkoitus} />
                </div>
              </div>
              <div
                class="tarkennettu-row w-full mx-auto flex flex-col lg:flex-row items-center">
                <div class="w-full lg:w-1/2">
                  <InfoTooltip
                    tooltip={$_('TILASTOT_RAKENNUSVUOSI_TOOLTIP')}
                    title={$_('TILASTOT_RAKENNUSVUOSI')}>
                    <span
                      class="tarkennettu-label text-ashblue tracking-widest">
                      {$_('TILASTOT_RAKENNUSVUOSI')}
                    </span>
                  </InfoTooltip>
                </div>
                <div class="w-full lg:w-1/2 flex justify-between items-center">
                  <div class="w-2/5">
                    <InputNumber
                      bind:this={vuosiminInput}
                      label={$_('TILASTOT_RAKENNUSVUOSI')}
                      placeholder={'vvvv'}
                      min={0}
                      max={3000}
                      model={searchmodel}
                      name={'vuosimin'}
                      step="1"
                      invalidMessage={$_('TILASTOT_INVALID_VUOSI_MIN')} />
                  </div>
                  <span class="material-icons text-darkgrey" aria-hidden="true">
                    horizontal_rule
                  </span>
                  <div class="w-2/5">
                    <InputNumber
                      bind:this={vuosimaxInput}
                      label={$_('TILASTOT_RAKENNUSVUOSI')}
                      placeholder={'vvvv'}
                      min={0}
                      max={3000}
                      model={searchmodel}
                      name={'vuosimax'}
                      step="1"
                      invalidMessage={$_('TILASTOT_INVALID_VUOSI_MAX')} />
                  </div>
                </div>
              </div>
              <div
                class="tarkennettu-row w-full mx-auto flex flex-col lg:flex-row items-center">
                <div class="w-full lg:w-1/2">
                  <InfoTooltip
                    tooltip={$_('TILASTOT_LAMMITETTY_NETTOALA_TOOLTIP')}
                    title={$_('TILASTOT_LAMMITETTY_NETTOALA')}>
                    <span
                      class="tarkennettu-label text-ashblue tracking-widest">
                      {$_('TILASTOT_LAMMITETTY_NETTOALA')}
                    </span>
                  </InfoTooltip>
                </div>
                <div class="w-full lg:w-1/2 flex justify-between items-center">
                  <div class="w-2/5">
                    <InputNumber
                      bind:this={nettoalaminInput}
                      label={$_('TILASTOT_LAMMITETTY_NETTOALA')}
                      placeholder={'m²'}
                      min="0"
                      max={searchmodel.nettoalamax || 999999}
                      model={searchmodel}
                      name={'nettoalamin'}
                      step="1"
                      invalidMessage={$_('TILASTOT_INVALID_NETTOALA_MIN')} />
                  </div>
                  <span class="material-icons text-darkgrey" aria-hidden="true">
                    horizontal_rule
                  </span>
                  <div class="w-2/5">
                    <InputNumber
                      bind:this={nettoalamaxInput}
                      label={$_('TILASTOT_LAMMITETTY_NETTOALA')}
                      placeholder={'m²'}
                      min={searchmodel.nettoalamin || 0}
                      max={999999}
                      model={searchmodel}
                      name={'nettoalamax'}
                      step="1"
                      invalidMessage={$_('TILASTOT_INVALID_NETTOALA_MAX')} />
                  </div>
                </div>
              </div>
            </div>
            <!-- BUTTONS -->
            <div class="w-full lg:w-11/12 mt-4 flex flex-col sm:flex-row">
              <Button type={'submit'} {...buttonStyles.green}>
                {$_('TILASTOT_HAE')}
              </Button>
              <Button type={'reset'} {...buttonStyles.ashblue}>
                {$_('TILASTOT_TYHJENNA')}
              </Button>
            </div>
          {:catch}
            <div class="my-4"><span>{$_('SERVER_ERROR')}</span></div>
          {/await}
        </form>
      </div>
      <div
        class="statistics flex flex-col w-full my-8 print:my-0"
        bind:this={resultsElem}>
        {#if searchCommitted}
          {#await tilastotSearchPromise}
            <div class="flex justify-center">
              <Spinner />
            </div>
          {:then search}
            <div class="results">
              <!-- RESULT COUNT-->
              <span class="uppercase font-bold w-full mb-2">
                {$_('TILASTOT_TULOKSIA')}
                {' '}
                {total2013 + total2018 || '< 4'}
              </span>
              <!-- SEARCH PARAMS SHOWN WHEN PRINTING -->
              <div class="only-print">
                <div class="w-full flex flex-col space-y-1 text-sm">
                  {#if resultKeyword}
                    <div class="w-full space-x-2">
                      <span class="font-bold">{$_('TILASTOT_ALUE')}</span>
                      <span>{resultKeyword}</span>
                    </div>
                  {/if}
                  {#if resultKayttotarkoitus}
                    {#await kayttotarkoituksetPromise then kayttotarkoitukset}
                      <div class="w-full space-x-2">
                        <span class="font-bold">{$_('TILASTOT_TYYPPI')}</span>
                        <span
                          >{labelLocale(
                            $locale,
                            kayttotarkoitukset.find(
                              item =>
                                item.id === parseInt(resultKayttotarkoitus, 10)
                            )
                          )}</span>
                      </div>
                    {/await}
                  {:else}
                    <div class="w-full space-x-2">
                      <span class="font-bold">{$_('TILASTOT_TYYPPI')}</span>
                      <span>{$_('KAIKKI')}</span>
                    </div>
                  {/if}

                  {#if resultVuosimin || resultVuosimax}
                    <div class="w-full space-x-2">
                      <span class="font-bold">
                        {$_('TILASTOT_RAKENNUSVUOSI')}
                      </span>
                      <span>{resultVuosimin} - {resultVuosimax}</span>
                    </div>
                  {/if}
                  <div class="w-full space-x-2">
                    <span class="font-bold"
                      >{$_('TILASTOT_LAMMITETTY_NETTOALA_T')}</span>
                    <span
                      >{`${resultNettoalamin || 0} m² - ${
                        resultNettoalamax || '∞'
                      } m²`}</span>
                  </div>
                </div>
              </div>
              {#if total2013 + total2018 > 0}
                <!-- GRAPHS -->
                <div
                  class="my-4 flex flex-col lg:flex-row space-y-4 lg:space-x-16 lg:space-y-0 justify-evenly">
                  <TilastotEtVersion
                    {printing}
                    version="2018"
                    count={total2018}
                    eLukuData={results?.['e-luku-statistics']?.['2018']}
                    chartData={chartData2018} />
                  <TilastotEtVersion
                    {printing}
                    tooltipAnchorPosition={AnchorPosition.bottomRight}
                    version="2013"
                    count={total2013}
                    eLukuData={results?.['e-luku-statistics']?.['2013']}
                    chartData={chartData2013} />
                </div>
                <!-- MOLEMMILLE TUNNUSLUVUT-->
                <div class="pbb-always">
                  <h1 class="w-full mt-6 mb-2 space-x-2">
                    {$_('TILASTOT_TUNNUSLUVUT_MOLEMMILLE')}
                    {` (${total2013 + total2018} ${$_('TILASTOT_KPL')})`}
                  </h1>
                  <div
                    class="flex flex-col lg:flex-row space-y-4 lg:space-x-16 lg:space-y-0 justify-evenly">
                    <div class="w-full flex flex-col">
                      <div class="my-4 w-full">
                        <InfoTooltip
                          title={$_('TILASTOT_RAKENNUSVAIPPA')}
                          tooltip={$_('TILASTOT_RAKENNUSVAIPPA_TOOLTIP')}>
                          <h2>{$_('TILASTOT_RAKENNUSVAIPPA')}</h2>
                        </InfoTooltip>
                      </div>
                      <div class="w-full flex flex-col">
                        <dl class="w-full flex flex-col">
                          <div class="w-full flex justify-between">
                            <dt>
                              <InfoTooltip
                                title={$_('TILASTOT_ILMANVUOTOLUKU')}
                                tooltip={$_('TILASTOT_ILMANVUOTOLUKU_TOOLTIP')}>
                                <span
                                  >{$_('TILASTOT_ILMANVUOTOLUKU')}{' q'}<sub
                                    >50</sub
                                  ></span>
                              </InfoTooltip>
                            </dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['ilmanvuotoluku']
                              )}
                            </dd>
                          </div>
                        </dl>
                        <div class="mt-4">
                          <InfoTooltip
                            title={$_('TILASTOT_U_ARVOT')}
                            tooltip={$_('TILASTOT_U_ARVOT_TOOLTIP')}>
                            <span class="font-bold"
                              >{$_('TILASTOT_U_ARVOT')}</span>
                          </InfoTooltip>
                        </div>
                        <dl>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_ULKOSEINAT')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['ulkoseinat-u']
                              )}{$_('TILASTOT_U_ARVOT_UNIT')}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_YLAPOHJA')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['ylapohja-u']
                              )}{$_('TILASTOT_U_ARVOT_UNIT')}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_ALAPOHJA')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['alapohja-u']
                              )}{$_('TILASTOT_U_ARVOT_UNIT')}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_IKKUNAT')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['ikkunat-u']
                              )}{$_('TILASTOT_U_ARVOT_UNIT')}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_ULKOOVET')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['ulkoovet-u']
                              )}{$_('TILASTOT_U_ARVOT_UNIT')}
                            </dd>
                          </div>
                        </dl>
                      </div>
                    </div>
                    <div class="w-full flex flex-col space-y-2">
                      <div class="w-full flex flex-col">
                        <h2 class="my-4 text-green">
                          {$_('TILASTOT_LAMMITYS')}
                        </h2>
                        <dl>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_VARAAVIEN')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.['takka']
                              )}{$_('TILASTOT_KPL')}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_LISALAMPOPUMPPOJEN')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.[
                                  'ilmalampopumppu'
                                ]
                              )}{$_('TILASTOT_KPL')}
                            </dd>
                          </div>
                        </dl>

                        <div class="mt-4">
                          <InfoTooltip
                            anchor={AnchorPosition.bottomRight}
                            title={$_('TILASTOT_LAMPOPUMPUN')}
                            tooltip={$_('TILASTOT_LAMPOPUMPUN_TOOLTIP')}>
                            <span>{$_('TILASTOT_LAMPOPUMPUN')}</span>
                          </InfoTooltip>
                        </div>

                        <dl>
                          <div class="w-full flex justify-between pl-4">
                            <dt>{$_('TILASTOT_TILOJEN')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.[
                                  'tilat-ja-iv-lampokerroin'
                                ]
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between pl-4">
                            <dt>{$_('TILASTOT_LAMPIMAN')}</dt>
                            <dd>
                              {format(
                                results?.['common-averages']?.[
                                  'lammin-kayttovesi-lampokerroin'
                                ]
                              )}
                            </dd>
                          </div>
                        </dl>

                        <h2 class="my-4 text-green">
                          {$_('TILASTOT_ILMANVAIHTO')}
                        </h2>
                        <dl>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_LAMMONTALTEENOTON')}</dt>
                            <dd class="whitespace-no-wrap">
                              {formatPercent(
                                results?.['common-averages']?.[
                                  'lto-vuosihyotysuhde'
                                ]
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>
                              <InfoTooltip
                                anchor={AnchorPosition.bottomRight}
                                title={$_('TILASTOT_SFP_LUKU')}
                                tooltip={$_('TILASTOT_SFP_LUKU_TOOLTIP')}>
                                <span>{$_('TILASTOT_SFP_LUKU')}</span>
                              </InfoTooltip>
                            </dt>
                            <dd class="whitespace-no-wrap">
                              {format(
                                results?.['common-averages']?.[
                                  'ivjarjestelma-sfp'
                                ]
                              )}{' kW/(m³/s)'}
                            </dd>
                          </div>
                        </dl>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 2018 TUNNUSLUVUT-->
                {#if total2018 > 0}
                  <div>
                    <h1 class="w-full mt-6 mb-2 pbb-always">
                      {$_('TILASTOT_TUNNUSLUVUT_2018')}
                      {` (${total2018} ${$_('TILASTOT_KPL')})`}
                    </h1>
                    <div
                      class="flex flex-col lg:flex-row space-y-4 lg:space-x-16 lg:space-y-0 justify-evenly">
                      <TilastotEntriesList
                        title={$_('TILASTOT_LAMMITYSJARJESTELMA')}
                        tooltip={$_('TILASTOT_LAMMITYSJARJESTELMA_TOOLTIP')}
                        labels={lammitysmuodot}
                        items={results?.['counts']?.['2018']?.['lammitysmuoto']}
                        total={total2018} />
                      <TilastotEntriesList
                        title={$_('TILASTOT_ILMANVAIHTOJARJESTELMA')}
                        tooltip={$_('TILASTOT_ILMANVAIHTOJARJESTELMA_TOOLTIP')}
                        tooltipAnchor={AnchorPosition.bottomRight}
                        labels={ilmanvaihtotyypit}
                        items={results?.['counts']?.['2018']?.['ilmanvaihto']}
                        total={total2018} />
                    </div>
                    <div
                      class="flex flex-col lg:flex-row space-y-4 lg:space-x-16 lg:space-y-0 justify-evenly">
                      <div class="w-full flex flex-col">
                        <div class="my-4">
                          <InfoTooltip
                            title={$_('TILASTOT_UUSIUTUVIEN')}
                            tooltip={$_('TILASTOT_UUSIUTUVIEN_TOOLTIP')}>
                            <h2>{$_('TILASTOT_UUSIUTUVIEN')}</h2>
                          </InfoTooltip>
                        </div>
                        <dl>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_AURINKOSAHKO')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['aurinkosahko']
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_AURINKOLAMPO')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['aurinkolampo']
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_TUULISAHKO')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['tuulisahko']
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_LAMPOPUMPPU')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['lampopumppu']
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_MUU_SAHKO')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['muusahko']
                              )}
                            </dd>
                          </div>
                          <div class="w-full flex justify-between">
                            <dt>{$_('TILASTOT_MUU_LAMPO')}</dt>
                            <dd class="whitespace-no-wrap">
                              {parseAndFormatPercent(
                                total2018,
                                results?.[
                                  'uusiutuvat-omavaraisenergiat-counts'
                                ]?.['2018']?.['muulampo']
                              )}
                            </dd>
                          </div>
                        </dl>
                        <div class="w-full" />
                      </div>
                      <div class="w-full flex flex-col">
                        <!-- This empty div is here to keep the lonely last section the same size as the previous paired sections in large desktop windows -->
                      </div>
                    </div>
                  </div>
                {/if}
              {/if}
              <!-- PRINT -->
              <div class="w-full mx-auto my-8">
                <Button
                  {...buttonStyles.green}
                  on:click={() => {
                    printing = true;
                    tick().then(() => {
                      window.print();
                    });
                  }}>
                  <span class="material-icons align-middle" aria-hidden="true"
                    >print</span>
                  <span class="whitespace-no-wrap">
                    {$_('TILASTOT_TULOSTA')}
                  </span>
                </Button>
              </div>
            </div>
          {:catch}
            <span>{$_('SERVER_ERROR')}</span>
          {/await}
        {/if}
      </div>
    </div>
  </Container>
</div>
