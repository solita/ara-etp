<script>
  import { announcementsForModule } from '@Utility/announce';
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Parsers from '@Utility/parsers';
  import * as Selects from '@Component/Select/select-util';
  import * as Query from '@Utility/query';
  import qs from 'qs';

  import { querystring, location } from 'svelte-spa-router';
  import * as Router from '@Component/Router/router';

  import * as ViestiApi from './viesti-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Viestiketju from './viestiketju';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import Select2 from '@Component/Select/Select2';
  import Input from '@Component/Input/Input';
  import { announcePolitely } from '@Utility/aria-live';

  const i18n = $_;
  const { announceError, announceSuccess } = announcementsForModule('viesti');

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  let ketjutCount = 0;

  const announceSearchResults = ketjutCount => {
    announcePolitely(
      i18n('viesti.all.screen-reader.search-results', {
        values: { count: ketjutCount }
      })
    );
  };

  const defaultQuery = {
    page: Maybe.None(),
    'from-id': Maybe.None(),
    'vastaanottaja-id': Maybe.None(),
    'kasittelija-id': Maybe.None(),
    'has-kasittelija': Maybe.None(),
    valvonta: Maybe.None(),
    keyword: Maybe.None(),
    'include-kasitelty': Maybe.None()
  };

  const parseQuery = R.compose(
    R.mergeRight(defaultQuery),
    R.evolve({
      page: Query.parseInteger,
      'from-id': Query.parseInteger,
      'vastaanottaja-id': Query.parseInteger,
      'kasittelija-id': Query.parseInteger,
      'include-kasitelty': Query.parseBoolean,
      'has-kasittelija': Query.parseBoolean,
      valvonta: Query.parseBoolean,
      keyword: Parsers.optionalString
    }),
    qs.parse
  );

  const queryWindow = query => ({
    offset: R.map(R.compose(R.multiply(pageSize), R.dec), query.page),
    limit: Maybe.Some(pageSize)
  });

  const queryToBackendParams = R.compose(
    R.omit(['page']),
    query => R.mergeLeft(queryWindow(query), query),
    R.evolve({
      'has-kasittelija': R.filter(R.not),
      valvonta: R.filter(R.identity),
      keyword: R.map(txt => `%${txt}%`)
    })
  );

  const load = query => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            'viesti.all.messages.load-error',
            Response.localizationKey(response)
          )
        );
        announceError(msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        announceSearchResults(response.ketjutCount.count);
        ketjutCount = response.ketjutCount.count;
        overlay = false;
      },
      R.chain(
        whoami =>
          Future.parallelObject(4, {
            whoami: Future.resolve(whoami),
            ketjutCount: ViestiApi.getKetjutCount(
              Kayttajat.isLaatija(whoami)
                ? {}
                : R.compose(
                    R.omit(['offset', 'limit']),
                    queryToBackendParams
                  )(query)
            ),
            ketjut: ViestiApi.getKetjut(
              Kayttajat.isLaatija(whoami) ? {} : queryToBackendParams(query)
            ),
            osapuolet: Kayttajat.isLaatija(whoami)
              ? Future.resolve([])
              : ViestiApi.osapuolet,
            vastaanottajaryhmat: ViestiApi.vastaanottajaryhmat,
            kasittelijat: ViestiApi.getKasittelijat
          }),
        KayttajaApi.whoami
      )
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

  $: pageCount = Math.ceil(R.divide(ketjutCount, pageSize));
  const nextPageCallback = nextPage => {
    query = R.assoc('page', Maybe.fromNull(nextPage), query);
  };

  const submitKasitelty = (ketjuId, kasitelty) => {
    updateKetju(ketjuId, {
      kasitelty: kasitelty
    });
  };

  const updateKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `viesti.all.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        announceError(msg);
        overlay = false;
      },
      _ => {
        announceSuccess(i18n(`viesti.all.messages.update-success`));
        overlay = false;
        load(query);
      }
    ),
    R.tap(() => {
      overlay = true;
    }),
    ViestiApi.putKetju(fetch)
  );

  let cancelKeywordSearch = () => {};
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami, vastaanottajaryhmat, kasittelijat, osapuolet }}
      <div class="flex justify-between">
        <H1 text={i18n('viesti.all.title')} />
        <div class="font-bold">
          <Link
            icon={Maybe.Some('add_circle_outline')}
            text={i18n('viesti.all.new-viesti')}
            href="#/viesti/new" />
        </div>
      </div>
      {#if !Kayttajat.isLaatija(whoami)}
        <div class="flex justify-between mb-4">
          <div class="flex flex-grow">
            <div class="w-1/4">
              <Select
                compact={true}
                label={i18n('viesti.kasittelija')}
                bind:model={query}
                lens={R.lensProp('kasittelija-id')}
                items={R.pluck('id', kasittelijat)}
                format={Kayttajat.format(
                  i18n('viesti.mina'),
                  kasittelijat,
                  whoami
                )}
                parse={Maybe.Some}
                allowNone={true} />
            </div>
            <Checkbox
              id={'checkbox.no-handler'}
              name={'checkbox.no-handler'}
              label={i18n('viesti.all.no-handler')}
              lens={R.lensProp('has-kasittelija')}
              bind:model={query}
              format={R.compose(R.not, Maybe.orSome(false))}
              parse={R.compose(Maybe.Some, R.not)}
              disabled={false} />
          </div>
          <Checkbox
            id={'checkbox.show-handled'}
            name={'checkbox.show-handled'}
            label={i18n('viesti.all.show-handled')}
            lens={R.lensProp('include-kasitelty')}
            bind:model={query}
            format={R.compose(Maybe.orSome(false))}
            parse={R.compose(Maybe.Some)}
            disabled={false} />
        </div>

        <div class="flex md:flex-row flex-col">
          <div class="md:w-1/2 w-full mr-4 my-4">
            <Select2
              id={'viesti.from-id'}
              name={'viesti.from-id'}
              label={i18n('viesti.all.from-id')}
              bind:model={query}
              lens={R.lensProp('from-id')}
              modelToItem={R.chain(Maybe.findById(R.__, osapuolet))}
              itemToModel={R.map(R.prop('id'))}
              format={Maybe.fold(
                i18n('validation.no-selection'),
                Kayttajat.fullName
              )}
              items={Selects.addNoSelection(osapuolet)}
              searchable={true} />
          </div>

          <div class="md:w-1/2 w-full my-4">
            <Select2
              id={'viesti.vastaanottaja-id'}
              name={'viesti.vastaanottaja-id'}
              label={i18n('viesti.all.vastaanottaja-id')}
              bind:model={query}
              lens={R.lensProp('vastaanottaja-id')}
              modelToItem={R.chain(Maybe.findById(R.__, osapuolet))}
              itemToModel={R.map(R.prop('id'))}
              format={Maybe.fold(
                i18n('validation.no-selection'),
                Kayttajat.fullName
              )}
              items={Selects.addNoSelection(osapuolet)}
              searchable={true} />
          </div>
        </div>

        <div class="flex md:flex-row flex-col mb-8">
          <div class="md:w-1/2 w-full mr-4 my-4">
            <Input
              id="keyword"
              name="keyword"
              label={i18n('viesti.all.keyword-search')}
              model={query}
              lens={R.lensProp('keyword')}
              format={Maybe.orSome('')}
              parse={Parsers.optionalString}
              search={true}
              on:input={evt => {
                cancelKeywordSearch();
                cancelKeywordSearch = Future.value(
                  keyword => {
                    query = R.assoc('keyword', keyword, query);
                  },
                  Future.after(1000, Maybe.fromEmpty(R.trim(evt.target.value)))
                );
              }} />
          </div>
          <div class="md:w-1/2 w-full mr-4 my-4 md:my-8">
            <Checkbox
              id={'valvonta'}
              name={'valvonta'}
              label={i18n('viesti.all.valvonta')}
              lens={R.lensProp('valvonta')}
              bind:model={query}
              format={Maybe.orSome(false)}
              parse={Maybe.Some} />
          </div>
        </div>
      {/if}
      {#if ketjut.length === 0}
        <div>
          <span>{i18n('viesti.all.no-messages')}</span>
        </div>
      {/if}
      <div class="my-6">
        {#each ketjut as ketju}
          <Viestiketju
            {ketju}
            {whoami}
            {vastaanottajaryhmat}
            {kasittelijat}
            {submitKasitelty} />
        {/each}
      </div>
    {/each}
    {#if R.gt(pageCount, 1)}
      <div class="pagination">
        <Pagination
          {pageCount}
          pageNum={R.compose(Maybe.orSome(1), R.prop('page'))(query)}
          {nextPageCallback}
          itemsPerPage={pageSize}
          itemsCount={ketjutCount} />
      </div>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
