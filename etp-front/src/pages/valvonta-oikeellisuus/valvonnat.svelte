<script>
  import * as R from 'ramda';
  import * as dfns from 'date-fns';
  import * as Parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Query from '@Utility/query';
  import * as Locales from '@Language/locale-utils';
  import * as Selects from '@Component/Select/select-util';

  import { querystring, location } from 'svelte-spa-router';
  import * as Router from '@Component/Router/router';
  import qs from 'qs';

  import * as Toimenpiteet from '@Pages/valvonta-oikeellisuus/toimenpiteet';
  import * as Valvojat from '@Pages/valvonta/valvojat';

  import * as ETViews from '@Pages/energiatodistus/views';
  import * as Links from './links';

  import * as api from './valvonta-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as EnergiatodistusApi from '@Pages/energiatodistus/energiatodistus-api';

  import { _, locale } from '@Language/i18n';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Address from '@Pages/energiatodistus/address';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import Select2 from '@Component/Select/Select2';
  import Link from '@Component/Link/Link.svelte';
  import Input from '@Component/Input/Input';
  import RakennuksenNimi from '@Pages/energiatodistus/RakennuksenNimi';
  import { announcementsForModule } from '@Utility/announce';
  import { announcePolitely } from '@Utility/aria-live';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.all';
  const { announceError } = announcementsForModule('valvonta-oikeellisuus');

  const announceSearchResults = valvonnatCount => {
    announcePolitely(
      i18n(i18nRoot + '.screen-reader.search-results', {
        values: { count: valvonnatCount }
      })
    );
  };

  let textCancel = () => {};

  const defaultQuery = {
    page: Maybe.None(),
    'valvoja-id': Maybe.None(),
    'laatija-id': Maybe.None(),
    'include-closed': Maybe.None(),
    'has-valvoja': Maybe.None(),
    keyword: Maybe.None(),
    'toimenpidetype-id': Maybe.None(),
    'kayttotarkoitus-id': Maybe.None()
  };

  const parseQuery = R.compose(
    R.mergeRight(defaultQuery),
    R.evolve({
      page: Query.parseInteger,
      'valvoja-id': Query.parseInteger,
      'laatija-id': Query.parseInteger,
      'include-closed': Query.parseBoolean,
      'has-valvoja': Query.parseBoolean,
      keyword: Parsers.optionalString,
      'toimenpidetype-id': Query.parseInteger,
      'kayttotarkoitus-id': Query.parseInteger
    }),
    qs.parse
  );

  const wrapPercent = q => `%${q}%`;

  const queryToBackendParams = R.compose(
    R.omit(['page']),
    query =>
      R.mergeLeft(
        {
          offset: R.map(R.compose(R.multiply(pageSize), R.dec), query.page),
          limit: Maybe.Some(pageSize)
        },
        query
      ),
    R.evolve({
      keyword: R.map(wrapPercent),
      'has-valvoja': R.filter(R.not)
    })
  );

  const load = query => {
    overlay = true;
    Future.fork(
      response => {
        announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        announceSearchResults(response.count);
        overlay = false;
      },
      Future.parallelObject(5, {
        whoami: kayttajaApi.whoami,
        count: R.compose(
          R.map(R.prop('count')),
          api.valvontaCount,
          R.omit(['offset', 'limit']),
          queryToBackendParams
        )(query),
        luokittelut: EnergiatodistusApi.luokittelutAllVersions,
        toimenpidetyypit: api.toimenpidetyypit,
        kayttotarkoitukset: api.kayttotarkoitukset,
        valvojat: api.valvojat,
        valvonnat: api.valvonnat(queryToBackendParams(query)),
        laatijat: R.chain(
          whoami =>
            Kayttajat.isPaakayttaja(whoami)
              ? laatijaApi.laatijat
              : Future.resolve([]),
          kayttajaApi.whoami
        )
      })
    );
  };

  // use fixed location so that location is not reactive
  const originalLocation = $location;
  let loadedQuery = {};
  $: query = parseQuery($querystring);
  $: {
    // do not load if query is already loaded or the location is changed
    if (
      !R.equals(loadedQuery, query) &&
      R.equals(originalLocation, $location)
    ) {
      load(query);
      Router.push(originalLocation + Query.toQueryString(query));
      loadedQuery = query;
    }
  }

  const formatLaatija = kayttaja =>
    `${kayttaja.etunimi} ${kayttaja.sukunimi} | ${kayttaja.email}`;

  const nextPageCallback = nextPage =>
    (query = R.assoc('page', Maybe.Some(nextPage), query));

  const orEmpty = Maybe.orSome('');
  $: kayttotarkoitusTitle = ETViews.kayttotarkoitusTitle($locale);

  const isTodayDeadline = R.compose(
    EM.exists(dfns.isToday),
    R.prop('deadline-date')
  );

  const isPastDeadline = R.compose(
    EM.exists(R.both(R.complement(dfns.isToday), dfns.isPast)),
    R.prop('deadline-date')
  );

  const formatDeadline = R.compose(
    EM.fold('-', Formats.formatDateInstant),
    R.prop('deadline-date')
  );

  const toValvontaView = energiatodistus => {
    Router.push(Links.valvonta(energiatodistus));
  };

  const isLaatijaViesti = R.pathSatisfies(Kayttajat.isLaatijaRole, [
    'from',
    'rooli-id'
  ]);
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { valvonnat, count, whoami, luokittelut, toimenpidetyypit, valvojat, laatijat, kayttotarkoitukset }}
      <H1 text={i18n(i18nRoot + '.title')} />
      {#if Kayttajat.isPaakayttaja(whoami)}
        <div class="flex flex-wrap items-end md:space-y-0 space-y-4">
          <div class="w-1/4 mr-4">
            <Select
              disabled={overlay}
              compact={true}
              label={i18n(i18nRoot + '.valvoja')}
              bind:model={query}
              lens={R.lensProp('valvoja-id')}
              items={R.pluck(
                'id',
                R.filter(R.propEq(false, 'passivoitu'), valvojat)
              )}
              format={Kayttajat.format(i18n('valvonta.self'), valvojat, whoami)}
              parse={Maybe.Some}
              allowNone={true} />
          </div>
          <Checkbox
            disabled={overlay}
            label={i18n(`${i18nRoot}.ei-valvojaa`)}
            bind:model={query}
            lens={R.lensProp('has-valvoja')}
            format={R.compose(R.not, Maybe.orSome(false))}
            parse={R.compose(Maybe.Some, R.not)} />
          <div class="flex flex-grow md:justify-end">
            <Checkbox
              disabled={overlay}
              label={i18n(`${i18nRoot}.include-closed`)}
              bind:model={query}
              lens={R.lensProp('include-closed')}
              format={Maybe.orSome(false)}
              parse={Maybe.Some} />
          </div>
        </div>
        <div
          class="flex flex-wrap items-end md:space-x-4 md:space-y-0 space-y-4 my-4">
          <div class="md:w-1/2 w-full">
            <Input
              label={i18n(i18nRoot + '.keyword-search')}
              model={query}
              lens={R.lensProp('keyword')}
              format={Maybe.orSome('')}
              parse={Parsers.optionalString}
              search={true}
              on:input={evt => {
                textCancel();
                textCancel = Future.value(
                  keyword => {
                    query = R.assoc('keyword', keyword, query);
                  },
                  Future.after(1000, Maybe.fromEmpty(R.trim(evt.target.value)))
                );
              }} />
          </div>
          <div class="w-1/2 md:w-1/4">
            <Select
              disabled={overlay}
              compact={false}
              label={i18n(i18nRoot + '.last-toimenpide')}
              bind:model={query}
              lens={R.lensProp('toimenpidetype-id')}
              items={R.pluck('id', toimenpidetyypit)}
              format={id =>
                Locales.label(
                  $locale,
                  R.find(R.propEq(id, 'id'), toimenpidetyypit)
                )}
              parse={Maybe.Some}
              allowNone={true} />
          </div>
        </div>

        <div
          class="flex flex-wrap items-end md:space-x-4 md:space-y-0 space-y-4 my-4">
          <div class="md:w-1/2 w-full">
            <Select2
              id={'oikeellisuus.laatija'}
              name={'oikeellisuus.laatija'}
              label={i18n(i18nRoot + '.laatija')}
              bind:model={query}
              lens={R.lensProp('laatija-id')}
              modelToItem={R.chain(id => Maybe.findById(id, laatijat))}
              itemToModel={R.map(R.prop('id'))}
              format={Maybe.fold(
                i18n('validation.no-selection'),
                formatLaatija
              )}
              items={Selects.addNoSelection(laatijat)}
              searchable={true} />
          </div>

          <div class="w-1/2 md:w-1/4">
            <Select
              disabled={overlay}
              compact={false}
              label={i18n(i18nRoot + '.kayttotarkoitusluokka')}
              bind:model={query}
              lens={R.lensProp('kayttotarkoitus-id')}
              items={R.pluck('id', kayttotarkoitukset)}
              format={id =>
                Locales.label(
                  $locale,
                  R.find(R.propEq(id, 'id'), kayttotarkoitukset)
                )}
              parse={Maybe.Some}
              allowNone={true} />
          </div>
        </div>
      {/if}
      {#if valvonnat.length === 0}
        <div class="my-6">{i18n(i18nRoot + '.empty')}</div>
      {:else}
        <div class="my-6 overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {i18n('valvonta.valvoja')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.last-toimenpide')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.deadline-date')}
                </th>
                <th class="etp-table--th">
                  {i18n('energiatodistus.haku.sarakkeet.tunnus')}
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
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each valvonnat as valvonta}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toValvontaView(valvonta.energiatodistus)}>
                  <!-- valvonta -->
                  <td
                    class="etp-table--td"
                    class:font-bold={Valvojat.isSelfInValvonta(
                      whoami,
                      valvonta
                    )}
                    class:text-primary={Valvojat.isSelfInValvonta(
                      whoami,
                      valvonta
                    )}>
                    {Maybe.fold(
                      '-',
                      Kayttajat.format(i18n('valvonta.self'), valvojat, whoami),
                      valvonta['valvoja-id']
                    )}
                  </td>
                  {#each Maybe.toArray(valvonta.lastToimenpide) as toimenpide}
                    <td class="etp-table--td">
                      <div class="flex flex-wrap">
                        <div class="flex items-center mr-1">
                          {Locales.labelForId(
                            $locale,
                            toimenpidetyypit
                          )(toimenpide['type-id'])}
                          {#if Toimenpiteet.isDraft(toimenpide)}
                            ({i18n(i18nRoot + '.table.draft')})
                          {/if}
                        </div>
                        {#each Maybe.toArray(valvonta['last-viesti']) as viesti}
                          <span
                            title={Formats.formatTimeInstantMinutes(
                              viesti['sent-time']
                            ) +
                              ' / ' +
                              Kayttajat.fullName(viesti.from)}
                            class="text-primary text-lg"
                            class:font-icon={isLaatijaViesti(viesti)}
                            class:font-icon-outlined={!isLaatijaViesti(viesti)}>
                            {viesti.kasitelty ? 'mark_email_read' : 'mail'}
                          </span>
                        {/each}
                      </div>
                    </td>
                    <td
                      class="etp-table--td"
                      class:font-bold={R.anyPass([
                        isTodayDeadline,
                        isPastDeadline
                      ])(toimenpide)}
                      class:text-primary={isTodayDeadline(toimenpide)}
                      class:text-error={isPastDeadline(toimenpide)}>
                      {formatDeadline(toimenpide)}
                    </td>
                  {/each}
                  {#if Maybe.isNone(valvonta.lastToimenpide)}
                    <td class="etp-table--td">
                      {i18n('valvonta.oikeellisuus.pending')}
                    </td>
                    <td class="etp-table--td">-</td>
                  {/if}
                  <!-- energiatodistus -->
                  <td class="etp-table--td">{valvonta.energiatodistus.id}</td>
                  <td class="etp-table--td">
                    <RakennuksenNimi
                      energiatodistus={valvonta.energiatodistus} />
                  </td>
                  <td class="etp-table--td">
                    <Address
                      energiatodistus={valvonta.energiatodistus}
                      postinumerot={luokittelut.postinumerot} />
                  </td>
                  <td
                    class="etp-table--td"
                    title={kayttotarkoitusTitle(
                      luokittelut,
                      valvonta.energiatodistus
                    )}>
                    {orEmpty(
                      valvonta.energiatodistus.perustiedot.kayttotarkoitus
                    )}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(valvonta.energiatodistus['laatija-fullname'])}
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}

      {#if R.gt(count, pageSize)}
        <div class="pagination">
          <Pagination
            pageNum={Maybe.orSome(1, R.prop('page', query))}
            {nextPageCallback}
            itemsPerPage={pageSize}
            itemsCount={count} />
        </div>
      {/if}

      {#if Kayttajat.isPaakayttaja(whoami)}
        <ul>
          <li>
            <Link
              icon={Maybe.Some('download_for_offline')}
              href="api/private/valvonta/oikeellisuus/csv/valvonta.csv"
              text={i18n(i18nRoot + '.download-all')} />
          </li>

          <li>
            <Link
              icon={Maybe.Some('download_for_offline')}
              href="api/private/valvonta/oikeellisuus/virhetypes/statistics/virhetilastot.csv"
              text={i18n(i18nRoot + '.download-error-stats')} />
          </li>
        </ul>
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
