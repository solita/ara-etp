<script>
  import { onMount } from 'svelte';
  import { MD5 } from 'object-hash';
  import * as dfns from 'date-fns';
  import * as LaatijaUtils from '@/utilities/laatija';
  import { hashCode } from '@/utilities/strings';
  import * as GeoUtils from '@/utilities/geo';
  import * as FormUtils from '@/utilities/form';
  import { announceAssertively } from '@/utilities/announce';
  import { navigate } from '@/router/router';
  import { _, locale } from '@Localization/localization';
  import {
    laatijat as laatijatStore,
    patevyydet,
    toimintaalueet,
    postinumerot,
    kunnat
  } from '@/stores';

  import Button, { styles as buttonStyles } from '@Component/button';
  import Input from '@Component/input-search';
  import InfoBlock from '@Component/info-block';
  import TableLaatijahaku from '@Component/table-laatijahaku';
  import TableLaatijahakuFilter from '@Component/table-laatijahaku-filter';
  import Container, { styles as containerStyles } from '@Component/container';
  import Spinner from '@Component/spinner';
  import Pagination from '@Component/pagination';
  import Seo from '@Component/seo';

  export let nimihaku = '';
  export let aluehaku = '';
  export let page = 0;
  export let filterPatevyydet = '1,2';

  announceAssertively($_('NAVBAR_LAATIJAHAKU'));

  const dateText = dfns.format(new Date(), 'yyyy-MM-dd');

  const laatijaSort = ({ etunimi, sukunimi }) =>
    hashCode(MD5({ etunimi, sukunimi, dateText }));

  let resultsElement;
  $: {
    if (nimihaku || aluehaku || page > 0)
      resultsElement?.scrollIntoView({ behavior: 'smooth' });

    // ---
    // When navigating to laatijahaku from navbar link while filterPatevyydet is set as an URL parameter,
    // filterPatevyydet for some reason becomes undefined and stays undefined when making LaatijaUtils.laatijatByHakukriteerit call, causing a nasty error that breaks the site until full refresh
    // ---
    if (!filterPatevyydet) filterPatevyydet = '1,2';
  }
  const pageSize = 10;

  let shownLaatijat = new Promise(() => {});

  $: haetutToimintaalueet = Promise.all([
    Promise.resolve(aluehaku ?? ''),
    $toimintaalueet,
    $kunnat,
    $postinumerot
  ]).then(([...args]) => GeoUtils.findToimintaalueIds(...args));

  $: shownLaatijat = Promise.all([
    Promise.resolve(aluehaku ?? ''),
    Promise.resolve(nimihaku ?? ''),
    $laatijatStore,
    haetutToimintaalueet,
    Promise.resolve(filterPatevyydet)
  ]).then(
    ([aluehaku, nimihaku, laatijat, haetutToimintaalueet, filterPatevyydet]) =>
      LaatijaUtils.laatijatByHakukriteerit(
        aluehaku,
        nimihaku,
        laatijat,
        haetutToimintaalueet,
        filterPatevyydet
      ).sort((a, b) => {
        const wt =
          LaatijaUtils.calculateLaatijaWeight(haetutToimintaalueet, b) -
          LaatijaUtils.calculateLaatijaWeight(haetutToimintaalueet, a);
        if (wt !== 0) {
          return wt;
        }

        return laatijaSort(b) - laatijaSort(a);
      })
  );

  const commitSearch = (nimihaku, aluehaku) => {
    filterPatevyydet = '1,2';
    const qs = [
      ...(nimihaku ? [['nimihaku', nimihaku].join('=')] : []),
      ...(aluehaku ? [['aluehaku', aluehaku].join('=')] : []),
      ...[['filterPatevyydet', filterPatevyydet].join('=')],
      ...[['page', 0].join('=')]
    ].join('&');
    navigate(`/laatijahaku${qs ? '?' + qs : ''}`);
    haetutToimintaalueet = Promise.all([
      Promise.resolve(aluehaku ?? ''),
      $toimintaalueet,
      $kunnat,
      $postinumerot
    ]).then(([...args]) => GeoUtils.findToimintaalueIds(...args));
  };

  onMount(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    });
  });
</script>

<svelte:window
  on:popstate={_ => {
    haetutToimintaalueet = Promise.all([
      Promise.resolve(aluehaku ?? ''),
      $toimintaalueet,
      $kunnat,
      $postinumerot
    ]).then(([...args]) => GeoUtils.findToimintaalueIds(...args));
  }} />

<Seo
  nofollow={true}
  title="{$_('ENERGIATODISTUSREKISTERI')} - {$_('NAVBAR_LAATIJAHAKU')}"
  descriptionFi={$locale == 'fi' ? $_('NAVBAR_LAATIJAHAKU_KUVAUS') : undefined}
  descriptionSv={$locale == 'sv'
    ? $_('NAVBAR_LAATIJAHAKU_KUVAUS')
    : undefined} />

<Container {...containerStyles.beige}>
  <InfoBlock title={$_('LHAKU_INFO_TITLE')}>{$_('LHAKU_INFO_TEXT')}</InfoBlock>
</Container>
<Container {...containerStyles.white}>
  <div
    class="px-4 lg:px-8 xl:px-16 pt-8 pb-4 mx-auto flex flex-col md:flex-row items-center md:items-start">
    <div class="flex flex-col w-full md:w-7/12">
      <form
        on:submit|preventDefault={evt => {
          const fd = FormUtils.deserialize(evt.target);
          commitSearch(fd.nimi, fd.alue);
        }}
        on:reset|preventDefault={_ => commitSearch('', '')}>
        <div class="w-full md:w-11/12">
          <Input label={$_('LHAKU_HAE_NIMELLA')} name="nimi" value={nimihaku} />
        </div>
        <aside class="font-normal text-xs italic mt-4">
          {$_('LHAKU_HAE_ASIDE')}
        </aside>
        <div class="flex">
          <div class="w-full md:w-11/12">
            <Input
              label={$_('LHAKU_HAE_ALUEELLA')}
              name="alue"
              value={aluehaku} />
          </div>
        </div>
        <div class="w-full md:w-11/12 mt-4 flex flex-col sm:flex-row">
          <Button type={'submit'} {...buttonStyles.green}>{$_('HAE')}</Button>
        </div>
      </form>
    </div>
    <aside
      class="mt-4 md:mt-0 md:w-1/2 md:pl-4 md:border-l-8 border-ashblue text-ashblue italic text-sm">
      {$_('LHAKU_ASIDE')}
    </aside>
  </div>
</Container>

<Container {...containerStyles.white}>
  <div
    class="px-3 lg:px-8 xl:px-16 pb-8 flex flex-col w-full"
    bind:this={resultsElement}>
    {#await Promise.all( [shownLaatijat, haetutToimintaalueet, $patevyydet, Promise.resolve(parseInt(page ?? 0)), Promise.resolve(pageSize), Promise.resolve(filterPatevyydet)] )}
      <div class="flex justify-center">
        <Spinner />
      </div>
    {:then [l, h, patevyydet, page, pageSize, filterPatevyydet]}
      <TableLaatijahaku
        laatijaCount={l.length}
        laatijat={l.slice(page * pageSize, (page + 1) * pageSize)}
        let:currentPageItemCount
        haetutToimintaalueet={h}
        {patevyydet}
        {page}>
        <div slot="filter">
          <TableLaatijahakuFilter
            on:change={evt =>
              navigate(
                `/laatijahaku?${[
                  ...(nimihaku ? [['nimihaku', nimihaku].join('=')] : []),
                  ...(aluehaku ? [['aluehaku', aluehaku].join('=')] : []),
                  ...[['filterPatevyydet', evt.target.value].join('=')],
                  ...[['page', 0].join('=')]
                ].join('&')}`
              )}
            showPatevyydet={filterPatevyydet}
            {patevyydet} />
        </div>
        <div slot="pagination">
          <Pagination
            {page}
            {pageSize}
            {currentPageItemCount}
            itemCount={l.length}
            queryStringFn={page =>
              `/laatijahaku?${[
                ...(nimihaku ? [['nimihaku', nimihaku].join('=')] : []),
                ...(aluehaku ? [['aluehaku', aluehaku].join('=')] : []),
                ...[['filterPatevyydet', filterPatevyydet].join('=')],
                ...[['page', page].join('=')]
              ].join('&')}`} />
        </div>
      </TableLaatijahaku>
    {:catch error}
      <span>{$_('SERVER_ERROR')}</span>
    {/await}
  </div>
</Container>
