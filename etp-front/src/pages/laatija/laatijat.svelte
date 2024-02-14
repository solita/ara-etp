<script>
  import * as R from 'ramda';
  import * as qs from 'qs';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Locales from '@Language/locale-utils';
  import * as dfns from 'date-fns';
  import * as dfnstz from 'date-fns-tz';

  import * as LaatijaApi from '@Pages/laatija/laatija-api';
  import * as YritysApi from '@Pages/yritys/yritys-api';
  import * as GeoApi from '@Utility/api/geo-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import { replace, location, querystring } from 'svelte-spa-router';
  import { locale, _ } from '@Language/i18n';

  import Input from '@Component/Input/Input';
  import Datepicker from '@Component/Input/Datepicker.svelte';
  import H1 from '@Component/H/H1';
  import Select from '@Component/Select/Select';
  import Results from './laatijat-results.svelte';
  import Label from '@Component/Label/Label.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const i18nRoot = 'laatijat';
  const { announceError, announceSuccess, clearAnnouncements } =
    announcementsForModule('Laatija');
  let resources = Maybe.None();
  let overlay = true;

  // Load all page resources
  Future.fork(
    response => {
      announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(5, {
      laatijat: R.map(
        R.filter(R.complement(R.prop('partner'))),
        LaatijaApi.laatijat
      ),
      yritykset: YritysApi.getAllYritykset,
      patevyydet: LaatijaApi.patevyydet,
      toimintaalueet: GeoApi.toimintaalueet,
      whoami: KayttajaApi.whoami
    })
  );

  const filters = [
    R.allPass([R.prop('voimassa'), R.complement(R.prop('laatimiskielto'))]),
    R.propSatisfies(Maybe.isNone, 'login'),
    R.prop('laatimiskielto'),
    R.complement(R.prop('voimassa'))
  ];

  const parseInt = R.compose(Either.toMaybe, Parsers.parseInteger);

  const parseISODate = R.compose(Either.toMaybe, Parsers.parseISODate);

  const parseDate = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseDate),
    Parsers.optionalString
  );

  const formatFilter = state => i18n('laatijat.filters.' + state);

  const keywordSearch = search =>
    R.compose(
      R.any(R.includes(R.compose(R.toLower, R.trim)(search))),
      R.map(R.toLower),
      R.flatten,
      R.values,
      l => ({ ...l, etunimisukunimi: l.etunimi + ' ' + l.sukunimi }),
      R.pick([
        'etunimi',
        'sukunimi',
        'henkilotunnus',
        'email',
        'jakeluosoite',
        'postinumero',
        'postitoimipaikka',
        'puhelin'
      ]),
      R.evolve({
        henkilotunnus: Maybe.orSome([])
      })
    );

  const defaultQuery = {
    search: Maybe.None(),
    filter: Maybe.None(),
    'patevyystaso-id': Maybe.None(),
    'toimintaalue-id': Maybe.None(),
    'voimassaolo-paattymisaika-after': Maybe.None(),
    'voimassaolo-paattymisaika-before': Maybe.None(),
    page: 1
  };

  // parse query from querystring
  $: query = R.compose(
    R.mergeRight(defaultQuery),
    R.evolve({
      search: Maybe.fromEmpty,
      page: R.compose(Maybe.orSome(1), parseInt),
      filter: parseInt,
      'patevyystaso-id': parseInt,
      'toimintaalue-id': parseInt,
      'voimassaolo-paattymisaika-after': parseISODate,
      'voimassaolo-paattymisaika-before': parseISODate
    }),
    R.pick([
      'search',
      'page',
      'filter',
      'patevyystaso-id',
      'toimintaalue-id',
      'voimassaolo-paattymisaika-after',
      'voimassaolo-paattymisaika-before'
    ])
  )(qs.parse($querystring));

  // update querystring from query
  $: R.compose(
    querystring => replace(`${$location}?${querystring}`),
    qs.stringify,
    R.map(Maybe.orSome('')),
    R.filter(Maybe.isSome),
    R.evolve({
      page: R.compose(R.reject(R.equals(0)), Maybe.Some)
    })
  )(query);

  const isAfter = dateToCompare => date => dfns.isAfter(date, dateToCompare);
  const isBefore = dateToCompare => date => dfns.isBefore(date, dateToCompare);

  const propSatisfies = (binaryPredicate, name) =>
    R.compose(R.propSatisfies(R.__, name), binaryPredicate);

  // predicate from query
  const predicate = R.compose(
    R.allPass,
    R.values,
    R.evolve({
      search: keywordSearch,
      filter: R.nth(R.__, filters),
      'patevyystaso-id': R.propEq('patevyystaso'),
      'toimintaalue-id': R.compose(R.propEq('toimintaalue'), Maybe.Some),
      'voimassaolo-paattymisaika-after': R.compose(
        propSatisfies(isAfter, 'voimassaolo-paattymisaika'),
        d => dfnstz.zonedTimeToUtc(d, 'Europe/Helsinki')
      ),
      'voimassaolo-paattymisaika-before': R.compose(
        propSatisfies(isBefore, 'voimassaolo-paattymisaika'),
        d => dfnstz.zonedTimeToUtc(d, 'Europe/Helsinki'),
        // this is for inclusive end date range
        d => dfns.add(d, { days: 1, hours: 1 })
      )
    })
  );

  const filter = (query, laatijat) => {
    overlay = true;
    return Future.timeout(_ => {
      overlay = false;
      return R.filter(predicate(query), laatijat);
    }, 200);
  };

  const newFilterQuery = R.compose(
    R.map(Maybe.get),
    R.filter(Maybe.isSome),
    R.pick([
      'search',
      'filter',
      'patevyystaso-id',
      'toimintaalue-id',
      'voimassaolo-paattymisaika-after',
      'voimassaolo-paattymisaika-before'
    ])
  );

  let filterQuery = {};
  const updateSearch = query => {
    const newQuery = newFilterQuery(query);
    if (!R.equals(filterQuery, newQuery)) {
      filterQuery = newQuery;
    }
  };
  $: updateSearch(query);

  const toPage = nextPage => {
    query = R.assoc('page', nextPage, query);
  };
</script>

<div class="w-full mt-3">
  <H1 text={i18n(i18nRoot + '.title')} />

  <Overlay {overlay}>
    <div slot="content">
      {#each Maybe.toArray(resources) as { laatijat, yritykset, patevyydet, toimintaalueet, whoami }}
        <div class="flex flex-wrap">
          <div class="lg:w-1/3 w-full px-4 py-4">
            <Select
              label={i18n(i18nRoot + '.filters.label')}
              disabled={false}
              bind:model={query}
              lens={R.lensProp('filter')}
              format={formatFilter}
              parse={Maybe.Some}
              noneLabel={i18nRoot + '.filters.all'}
              items={R.range(0, R.length(filters))} />
          </div>

          <div class="lg:w-1/3 w-full px-4 py-4">
            <Select
              label={i18n(i18nRoot + '.patevyystaso')}
              disabled={false}
              bind:model={query}
              lens={R.lensProp('patevyystaso-id')}
              format={Locales.labelForId($locale, patevyydet)}
              parse={Maybe.Some}
              noneLabel={i18nRoot + '.filters.all'}
              items={R.pluck('id', patevyydet)} />
          </div>

          <div class="lg:w-1/3 w-full px-4 py-4">
            <Select
              label={i18n(i18nRoot + '.toimintaalue')}
              disabled={false}
              bind:model={query}
              lens={R.lensProp('toimintaalue-id')}
              format={Locales.labelForId($locale, toimintaalueet)}
              parse={Maybe.Some}
              noneLabel={i18nRoot + '.filters.all'}
              items={R.pluck('id', toimintaalueet)} />
          </div>

          <div class="lg:w-1/2 w-full px-4 py-4">
            <Input
              name="keyword-search"
              label={i18n(i18nRoot + '.keyword-search')}
              model={query}
              lens={R.lensProp('search')}
              format={Maybe.orSome('')}
              parse={Parsers.optionalString}
              search={true}
              on:input={evt => {
                query = R.assoc(
                  'search',
                  Maybe.Some(R.trim(evt.target.value)),
                  query
                );
              }} />
          </div>

          <div class="lg:w-1/2 w-full px-4 py-4 flex flex-wrap">
            <div class="w-full">
              <Label
                label={i18n(i18nRoot + '.voimassaolo-paattymisaika.label')} />
            </div>
            <div class="lg:w-1/3 w-full">
              <Datepicker
                label={i18n(i18nRoot + '.voimassaolo-paattymisaika.after')}
                compact="true"
                bind:model={query}
                lens={R.lensProp('voimassaolo-paattymisaika-after')}
                parse={parseDate}
                transform={Maybe.Some}
                format={Maybe.fold('', Formats.formatDateInstant)} />
            </div>
            <span class="w-min px-4 invisible lg:visible">-</span>
            <div class="lg:w-1/3 w-full">
              <Datepicker
                label={i18n(i18nRoot + '.voimassaolo-paattymisaika.before')}
                compact="true"
                bind:model={query}
                lens={R.lensProp('voimassaolo-paattymisaika-before')}
                parse={parseDate}
                transform={Maybe.Some}
                format={Maybe.fold('', Formats.formatDateInstant)} />
            </div>
          </div>
        </div>

        <div>
          <Results
            laatijatFuture={filter(filterQuery, laatijat)}
            page={query.page}
            {toPage}
            {yritykset}
            {patevyydet}
            {toimintaalueet}
            {whoami} />
        </div>
      {/each}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
</div>
