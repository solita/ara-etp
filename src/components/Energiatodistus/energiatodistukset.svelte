<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Formats from '@Utility/formats';
  import * as api from './energiatodistus-api';
  import * as et from './energiatodistus-utils';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';
  import Pagination from '@Component/Pagination/Pagination';

  import { flatSchema } from '@Component/energiatodistus-haku/schema';

  import { querystring } from 'svelte-spa-router';
  import qs from 'qs';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, currentUserStore } from '@/stores';
  import { push, replace } from '@Component/Router/router';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';
  import Select from '@Component/Select/Select';
  import Confirm from '@Component/Confirm/Confirm';
  import EnergiatodistusHaku from '@Component/energiatodistus-haku/energiatodistus-haku';

  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';
  import { jsonToString } from 'webpack/lib/Stats';

  let overlay = true;
  let failure = false;

  const pageSize = 20;
  let energiatodistukset = [];
  let energiatodistuksetCount = 0;
  $: pageCount = Math.ceil(R.divide(energiatodistuksetCount, pageSize));

  let cancel = () => {};

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');

  const formatTila = R.compose(
    Maybe.orSome($_('validation.no-selection')),
    Maybe.map(tila => $_(`energiatodistukset.tilat.` + tila)),
    R.when(R.complement(Maybe.isMaybe), Maybe.of)
  );

  const numberCheck = R.compose(
    R.filter(i => !isNaN(i)),
    R.map(i => parseInt(i, 10)),
    Maybe.fromNull
  );

  const toETView = (versio, id) => {
    push('#/energiatodistus/' + versio + '/' + id);
  };

  const removeEnergiatodistusFromList = R.curry((versio, id) =>
    R.filter(
      R.compose(
        R.not,
        R.allPass([R.propEq('versio', versio), R.propEq('id', id)])
      )
    )
  );

  const deleteEnergiatodistus = (versio, id) => {
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          $_('energiatodistukset.messages.delete-error')
        ),
      _ => {
        energiatodistukset = removeEnergiatodistusFromList(
          versio,
          id
        )(energiatodistukset);

        flashMessageStore.add(
          'Energiatodistus',
          'success',
          $_('energiatodistukset.messages.delete-success')
        );
        nextPageCallback(1);
      },
      api.deleteEnergiatodistus(fetch, versio, id)
    );
  };

  const parseQuerystring = R.compose(
    R.mergeRight({
      tila: Maybe.None(),
      where: [[]],
      keyword: Maybe.None(),
      id: Maybe.None(),
      page: Maybe.Some(0),
      order: Maybe.Some('desc'),
      sort: Maybe.Some('energiatodistus.id'),
      limit: Maybe.Some(pageSize),
      offset: Maybe.Some(0)
    }),
    R.evolve({
      tila: Maybe.fromNull,
      where: R.compose(Either.orSome([[]]), w =>
        Either.fromTry(() => JSON.parse(w))
      ),
      keyword: Maybe.fromNull,
      order: Maybe.fromNull,
      sort: Maybe.fromNull,
      limit: Maybe.fromNull,
      offset: Maybe.fromNull,
      page: numberCheck,
      id: numberCheck
    }),
    qs.parse
  );

  const parseQuerystringCount = R.compose(
    R.mergeLeft({
      order: Maybe.None(),
      sort: Maybe.None(),
      limit: Maybe.None(),
      offset: Maybe.None(),
      page: Maybe.None()
    }),
    R.mergeRight({
      tila: Maybe.None(),
      where: [[]],
      keyword: Maybe.None(),
      id: Maybe.None()
    }),
    R.evolve({
      tila: Maybe.fromNull,
      where: R.compose(Either.orSome([[]]), w =>
        Either.fromTry(() => JSON.parse(w))
      ),
      keyword: Maybe.fromNull,
      id: numberCheck
    }),
    qs.parse
  );

  const nextPageCallback = nextPage => {
    let param = R.compose(
      queryToQuerystring,
      R.mergeLeft({
        order: Maybe.None(),
        sort: Maybe.None(),
        limit: Maybe.None(),
        offset: Maybe.None()
      }),
      // R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema)),
      R.set(R.lensProp('page'), Maybe.Some(nextPage - 1)),
      parseQuerystring
    )(currentQuery);

    push(`#/energiatodistus/all${param}`);
  };

  $: parsedQuery = parseQuerystring($querystring);

  $: where = R.prop('where', parsedQuery);
  $: tila = R.prop('tila', parsedQuery);
  $: keyword = R.prop('keyword', parsedQuery);
  $: id = R.prop('id', parsedQuery);
  $: page = R.prop('page', parsedQuery);

  const queryToQuerystring = R.compose(
    api.toQueryString,
    R.pickBy(Maybe.isSome),
    R.evolve({
      where: R.compose(R.map(encodeURI), R.map(JSON.stringify), Maybe.fromEmpty)
    }),
    R.dissoc('id'),
    R.when(R.propSatisfies(Maybe.isSome, 'id'), q =>
      R.assoc(
        'where',
        EtHakuUtils.convertWhereToQuery(flatSchema, [
          [['=', 'id', Maybe.get(q.id)]]
        ]),
        q
      )
    ),
    R.defaultTo({
      tila: Maybe.None(),
      where: [[]],
      keyword: Maybe.None(),
      id: Maybe.None(),
      page: Maybe.None()
    })
  );

  let queryStringForXlsx;
  let currentQuery = 'where=[[]]';

  $: if ($querystring !== currentQuery) {
    currentQuery = $querystring;
    R.compose(
      Future.fork(
        () => {
          overlay = false;
          flashMessageStore.add(
            'Energiatodistus',
            'error',
            $_('energiatodistus.messages.load-error')
          );
        },
        response => {
          overlay = false;
          energiatodistukset = response[0];
        }
      ),
      Future.parallel(5),
      R.tap(toggleOverlay(true)),
      R.tap(cancel),
      R.prepend(R.__, [api.laatimisvaiheet]),
      api.getEnergiatodistukset,
      R.tap(queryString => queryStringForXlsx = queryString),
      queryToQuerystring,
      R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema)),
      R.set(
        R.lensProp('offset'),
        Maybe.Some(R.multiply(pageSize, page.orSome(0)))
      ),
      parseQuerystring
    )(currentQuery);

    R.compose(
      Future.fork(
        () => {
          flashMessageStore.add(
            'Energiatodistus',
            'error',
            $_('energiatodistus.messages.load-error')
          );
        },
        response => {
          energiatodistuksetCount = response.count;
        }
      ),
      api.getEnergiatodistuksetCount,
      queryToQuerystring,
      R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema)),
      parseQuerystringCount
    )(currentQuery);
  }
</script>

<style>
  .pagination:not(empty) {
    @apply mt-4;
  }

  .delete-icon:hover:not(.text-disabled) {
    @apply text-error;
  }

  .delete-icon.text-disabled {
    @apply cursor-not-allowed;
  }
</style>

<div class="w-full mt-3">
  <H1 text={$_('energiatodistukset.title')} />
  <div class="mb-10">
    <EnergiatodistusHaku
      {where}
      keyword={Maybe.orSome('', keyword)}
      id={Maybe.orSome('', id)} />
  </div>
  <Overlay {overlay}>
    <div slot="content">
      {#if R.isEmpty(energiatodistukset)}
        <p class="mb-10">
          Energiatodistuksia ei löydy annetuilla hakuehdoilla tai sinulle ei ole
          yhtään energiatodistusta.
        </p>
      {:else}
        <div class="mb-10">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.tila')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.tunnus')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.e-luokka')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.versio')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.voimassa')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.rakennuksen-nimi')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.osoite')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.ktl')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.laatija')}
                </th>
                <th class="etp-table--th  etp-table--th__center">
                  <span class="material-icons"> delete_forever </span>
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each energiatodistukset as energiatodistus}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toETView(energiatodistus.versio, energiatodistus.id)}>
                  <td class="etp-table--td">
                    {$_('energiatodistus.tila.' + et.tilaKey(energiatodistus['tila-id']))}
                  </td>
                  <td class="etp-table--td">{energiatodistus.id}</td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.tulokset['e-luokka'])}
                  </td>
                  <td class="etp-table--td">{energiatodistus.versio}</td>
                  <td class="etp-table--td">
                    {Maybe.fold('-',
                      Formats.inclusiveEndDate,
                      energiatodistus['voimassaolo-paattymisaika'])}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot.nimi)}
                  </td>
                  <td class="etp-table--td">
                    {`${orEmpty(energiatodistus.perustiedot['katuosoite-fi'])}, ${orEmpty(energiatodistus.perustiedot.postinumero)}`}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot.kayttotarkoitus)}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus['laatija-fullname'])}
                  </td>
                  <td class="etp-table--td etp-table--td__center">
                    <Confirm
                      let:confirm
                      confirmButtonLabel={$_('confirm.button.delete')}
                      confirmMessage={$_('confirm.you-want-to-delete')}>
                      <span
                        class="material-icons delete-icon"
                        class:text-disabled={et.tilaKey(energiatodistus['tila-id']) == 'signed'}
                        title={et.tilaKey(energiatodistus['tila-id']) == 'signed' ? $_('energiatodistus.haku.poista_disabled') : ''}
                        on:click|stopPropagation={_ => {
                          if (et.tilaKey(energiatodistus['tila-id']) != 'signed') confirm(deleteEnergiatodistus, energiatodistus.versio, energiatodistus.id);
                        }}>
                        highlight_off
                      </span>
                    </Confirm>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
          {#if R.gt(pageCount, 1)}
            <div class="pagination">
              <Pagination
                {pageCount}
                pageNum={page.orSome(0) + 1}
                {nextPageCallback}
                itemsPerPage={pageSize}
                itemsCount={energiatodistuksetCount} />
            </div>
          {/if}
        </div>
      {/if}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
  {#if Maybe.fold(false, KayttajaUtils.kayttajaHasAccessToResource([
      KayttajaUtils.laatijaRole
    ]), $currentUserStore)}
    <p class="mb-4">{$_('energiatodistus.et-lisays')}</p>
    <div class="mb-4 flex lg:flex-row flex-col">
      <div class="flex flex-row mb-4 mr-4">
        <span class="material-icons">add</span>
        &nbsp;
        <Link
          text={$_('energiatodistus.luo2018')}
          href="#/energiatodistus/2018/new" />
      </div>
      <div class="flex flex-row mb-4 mr-4">
        <span class="material-icons">add</span>
        &nbsp;
        <Link
          text={$_('energiatodistus.luo2013')}
          href="#/energiatodistus/2013/new" />
      </div>
    </div>
  {/if}
  <div class="flex flew-row mb-4 mr-4">
    <span class="material-icons">attachment</span>
    &nbsp;
    <Link
      text={$_('energiatodistus.lataa-xlsx')}
      href={'/api/private/energiatodistukset/xlsx/energiatodistukset.xlsx' + queryStringForXlsx} />
  </div>
</div>
