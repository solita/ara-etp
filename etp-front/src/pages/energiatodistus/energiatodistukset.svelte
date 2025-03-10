<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Response from '@Utility/response';
  import * as Query from '@Utility/query';
  import * as Locales from '@Language/locale-utils';

  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as et from '@Pages/energiatodistus/energiatodistus-utils';
  import * as ETViews from '@Pages/energiatodistus/views';
  import Pagination from '@Component/Pagination/Pagination';

  import { flatSchema } from '@Pages/energiatodistus/energiatodistus-haku/schema';

  import { querystring } from 'svelte-spa-router';
  import qs from 'qs';

  import { _, locale } from '@Language/i18n';
  import { push } from '@Component/Router/router';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';
  import Confirm from '@Component/Confirm/Confirm';
  import EnergiatodistusHaku from '@Pages/energiatodistus/energiatodistus-haku/energiatodistus-haku';
  import Address from '@Pages/energiatodistus/address';
  import RakennuksenNimi from '@Pages/energiatodistus/RakennuksenNimi';

  import * as EtHakuUtils from '@Pages/energiatodistus/energiatodistus-haku/energiatodistus-haku-utils';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import * as GeoApi from '@Utility/api/geo-api';
  import { announcementsForModule } from '@Utility/announce';
  import { announcePolitely } from '@Utility/aria-live';

  const i18n = $_;
  const { announceError, announceSuccess } =
    announcementsForModule('Energiatodistus');

  const announceSearchResults = etCount => {
    announcePolitely(
      i18n('energiatodistukset.screen-reader.search-results', {
        values: { count: etCount }
      })
    );
  };

  let overlay = true;
  let failure = false;

  const pageSize = 20;
  let energiatodistukset = [];
  let energiatodistuksetCount = 0;
  let resources = Maybe.None();
  $: pageCount = Math.ceil(R.divide(energiatodistuksetCount, pageSize));

  let cancel = () => {};

  const toggleOverlay = value => {
    overlay = value;
  };
  const orEmpty = Maybe.orSome('');

  const parseMaybeInt = R.compose(
    R.filter(i => !isNaN(i)),
    R.map(i => parseInt(i, 10)),
    Maybe.fromEmpty
  );

  const showError = defaultKey => response => {
    const msg = i18n(
      Maybe.orSome(defaultKey, Response.localizationKey(response))
    );

    announceError(msg);
  };

  const loadError = showError('energiatodistus.messages.load-error');

  const removeEnergiatodistusFromList = R.curry((versio, id) =>
    R.filter(
      R.compose(
        R.not,
        R.allPass([R.propEq(versio, 'versio'), R.propEq(id, 'id')])
      )
    )
  );

  const deleteEnergiatodistus = (versio, id) => {
    Future.fork(
      showError('energiatodistukset.messages.delete-error'),
      _ => {
        energiatodistukset = removeEnergiatodistusFromList(
          versio,
          id
        )(energiatodistukset);

        announceSuccess(i18n('energiatodistukset.messages.delete-success'));
        nextPageCallback(1);
      },
      api.deleteEnergiatodistus(fetch, versio, id)
    );
  };

  const parseQuerystring = R.compose(
    R.mergeRight({ where: [[]] }),
    R.mergeWith(Maybe.orElse, {
      keyword: Maybe.None(),
      id: Maybe.None(),
      page: Maybe.Some(0),
      limit: Maybe.Some(pageSize),
      order: Maybe.Some('desc'),
      sort: Maybe.Some('energiatodistus.id')
    }),
    R.evolve({
      where: R.compose(Either.orSome([[]]), w =>
        Either.fromTry(() => JSON.parse(w))
      ),
      keyword: Maybe.fromEmpty,
      order: Maybe.fromEmpty,
      sort: Maybe.fromEmpty,
      limit: Maybe.fromEmpty,
      page: parseMaybeInt,
      id: parseMaybeInt
    }),
    qs.parse
  );

  const nextPageCallback = nextPage => {
    let param = R.compose(
      queryToQuerystring,
      R.set(R.lensProp('page'), Maybe.Some(nextPage - 1)),
      parseQuerystring
    )(currentQuery);

    push(`#/energiatodistus/all${param}`);
  };

  $: parsedQuery = parseQuerystring($querystring);

  $: where = R.prop('where', parsedQuery);
  $: keyword = R.prop('keyword', parsedQuery);
  $: id = R.prop('id', parsedQuery);
  $: page = R.prop('page', parsedQuery);

  const queryToQuerystring = R.compose(
    Query.toQueryString,
    R.pickBy(Maybe.isSome),
    R.evolve({
      where: R.compose(R.map(JSON.stringify), Maybe.fromEmpty)
    }),
    R.dissoc('id'),
    R.when(R.propSatisfies(Maybe.isSome, 'id'), q =>
      R.assoc(
        'where',
        EtHakuUtils.convertWhereToQuery(flatSchema, [
          [['=', 'energiatodistus.id', Maybe.get(q.id)]]
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

  $: queryStringForExport = R.compose(
    queryToQuerystring,
    R.omit(['offset', 'page', 'limit']),
    R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema))
  )(parsedQuery);

  let currentQuery;

  const setOffsetToQuery = query =>
    R.assoc('offset', R.lift(R.multiply)(query.limit, query.page), query);

  $: if ($querystring !== currentQuery) {
    currentQuery = $querystring;
    cancel = R.compose(
      Future.fork(
        response => {
          toggleOverlay(false);
          loadError(response);
        },
        response => {
          toggleOverlay(false);
          energiatodistukset = response[0];
          energiatodistuksetCount = response[1].count;
          announceSearchResults(energiatodistuksetCount);
        }
      ),
      R.tap(() => toggleOverlay(true)),
      R.tap(cancel),
      Future.parallel(2),
      R.juxt([
        R.compose(
          R.map(
            R.map(R.evolve({ valvonta: { pending: Maybe.orSome(false) } }))
          ),
          api.getEnergiatodistukset,
          queryToQuerystring,
          R.dissoc('page'),
          setOffsetToQuery
        ),
        R.compose(
          api.getEnergiatodistuksetCount,
          queryToQuerystring,
          R.omit(['limit', 'sort', 'order', 'page', 'offset'])
        )
      ]),
      R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema)),
      parseQuerystring
    )(currentQuery);
  }

  // Load backend resources whoami & classifications
  Future.fork(
    loadError,
    response => {
      resources = Maybe.Some(response);
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      luokittelut: api.luokittelutAllVersions,
      toimenpidetyypit: ValvontaApi.toimenpidetyypit,
      kunnat: GeoApi.kunnat
    })
  );

  $: kayttotarkoitusTitle = ETViews.kayttotarkoitusTitle($locale);
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
  {#each resources.toArray() as { luokittelut, toimenpidetyypit, kunnat, whoami }}
    <div class="flex flex-col lg:flex-row justify-between">
      <H1 text={i18n('energiatodistukset.title')} />
      {#if Kayttajat.isLaatija(whoami)}
        <div
          class="mb-4 flex lg:flex-row flex-col lg:space-x-4 text-primary font-bold">
          <div class="flex flex-row my-auto">
            <Link
              text={i18n('energiatodistus.luo2018')}
              href="#/energiatodistus/2018/new"
              icon={Maybe.Some('add_circle_outline')} />
          </div>
          <div class="flex flex-row my-auto">
            <Link
              text={i18n('energiatodistus.luo2013')}
              href="#/energiatodistus/2013/new"
              icon={Maybe.Some('add_circle_outline')} />
          </div>
        </div>
      {/if}
    </div>
    <div class="mb-10">
      <EnergiatodistusHaku
        {where}
        {luokittelut}
        {whoami}
        {kunnat}
        keyword={Maybe.orSome('', keyword)}
        id={Maybe.orSome('', id)} />
    </div>
    <Overlay {overlay}>
      <div slot="content">
        {#if R.isEmpty(energiatodistukset)}
          <p class="mb-10">{i18n('energiatodistukset.not-found')}</p>
        {:else}
          <div class="mb-10 overflow-x-auto">
            <table class="etp-table">
              <thead class="etp-table--thead">
                <tr class="etp-table--tr etp-table--tr__light">
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.tila')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.tunnus')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.e-luokka')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.versio')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.voimassa')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.rakennuksen-nimi')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.osoite')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.ktl')}
                  </th>
                  <th class="etp-table--th">
                    {i18n('energiatodistus.haku.sarakkeet.laatija')}
                  </th>
                  {#if Kayttajat.isLaatija(whoami)}
                    <th class="etp-table--th etp-table--th__center">
                      <span class="material-icons"> delete_forever </span>
                    </th>
                  {/if}
                  {#if Kayttajat.isPaakayttaja(whoami)}
                    <th class="etp-table--th">
                      {i18n('energiatodistus.haku.sarakkeet.valvonta')}
                    </th>
                  {/if}
                </tr>
              </thead>
              <tbody class="etp-table--tbody">
                {#each energiatodistukset as energiatodistus}
                  <tr
                    data-cy="energiatodistus-row"
                    class="etp-table--tr etp-table--tr__link"
                    on:click={ETViews.toETView(energiatodistus)}>
                    <td data-cy="energiatodistus-tila" class="etp-table--td">
                      {i18n(
                        'energiatodistus.tila.' +
                          et.tilaKey(energiatodistus['tila-id'])
                      )}
                    </td>
                    <td data-cy="energiatodistus-id" class="etp-table--td">
                      {energiatodistus.id}
                    </td>
                    <td class="etp-table--td">
                      {orEmpty(energiatodistus.tulokset['e-luokka'])}
                    </td>
                    <td class="etp-table--td">{energiatodistus.versio}</td>
                    <td class="etp-table--td">
                      {Maybe.fold(
                        '-',
                        Formats.inclusiveEndDate,
                        energiatodistus['voimassaolo-paattymisaika']
                      )}
                    </td>
                    <td class="etp-table--td">
                      <RakennuksenNimi {energiatodistus} />
                    </td>
                    <td class="etp-table--td">
                      <Address
                        {energiatodistus}
                        postinumerot={luokittelut.postinumerot} />
                    </td>
                    <td
                      class="etp-table--td"
                      title={kayttotarkoitusTitle(
                        luokittelut,
                        energiatodistus
                      )}>
                      {orEmpty(energiatodistus.perustiedot.kayttotarkoitus)}
                    </td>
                    <td class="etp-table--td">
                      {orEmpty(energiatodistus['laatija-fullname'])}
                    </td>
                    {#if Kayttajat.isLaatija(whoami)}
                      <td
                        class="etp-table--td etp-table--td__center"
                        data-cy="energiatodistus-delete">
                        <Confirm
                          let:confirm
                          confirmButtonLabel={i18n('confirm.button.delete')}
                          confirmMessage={i18n('confirm.you-want-to-delete')}>
                          <span
                            class="material-icons delete-icon"
                            class:text-disabled={!et.isDraft(energiatodistus)}
                            title={!et.isDraft(energiatodistus)
                              ? i18n('energiatodistus.haku.poista-disabled')
                              : ''}
                            on:click|stopPropagation={_ => {
                              if (et.isDraft(energiatodistus))
                                confirm(
                                  deleteEnergiatodistus,
                                  energiatodistus.versio,
                                  energiatodistus.id
                                );
                            }}>
                            highlight_off
                          </span>
                        </Confirm>
                      </td>
                    {/if}
                    {#if Kayttajat.isPaakayttaja(whoami)}
                      <td class="etp-table--td">
                        {Maybe.fold(
                          energiatodistus.valvonta.pending
                            ? i18n('valvonta.oikeellisuus.pending')
                            : '',
                          Locales.labelForId($locale, toimenpidetyypit),
                          energiatodistus.valvonta['type-id']
                        )}
                      </td>
                    {/if}
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
    {#if Kayttajat.isPaakayttajaOrLaskuttaja(whoami)}
      <div class="flex flew-row mb-4 mr-4">
        <span class="material-icons">attachment</span>
        &nbsp;
        <Link
          text={i18n('energiatodistus.lataa-csv')}
          href={`/api/private/energiatodistukset/csv/energiatodistukset.csv${queryStringForExport}`} />
      </div>
    {/if}
    <div class="flex flew-row mb-4 mr-4">
      <span class="material-icons">attachment</span>
      &nbsp;
      <Link
        text={i18n('energiatodistus.lataa-xlsx')}
        href={`/api/private/energiatodistukset/xlsx/energiatodistukset.xlsx${queryStringForExport}`} />
    </div>
  {/each}
</div>
