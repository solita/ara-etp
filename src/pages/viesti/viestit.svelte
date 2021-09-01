<script>
  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import * as R from 'ramda';
  import * as Parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Kayttajat from '@Utility/kayttajat';
  import { querystring, location, replace } from 'svelte-spa-router';
  import qs from 'qs';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Viestiketju from './viestiketju';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';

  const i18n = $_;

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  let ketjutCount = 0;

  const queryStringIntegerProp = R.curry((querystring, prop) =>
    R.compose(
      R.chain(Either.toMaybe),
      R.map(Parsers.parseInteger),
      Maybe.fromEmpty,
      R.prop(prop)
    )(querystring)
  );

  const queryStringBooleanProp = R.curry((querystring, prop) =>
    R.compose(
      R.map(R.equals('true')),
      Maybe.fromEmpty,
      R.prop(prop)
    )(querystring)
  );

  let query = {
    page: Maybe.None(),
    'kasittelija-id': Maybe.None(),
    'has-kasittelija': Maybe.None(),
    'include-kasitelty': Maybe.None()
  };

  let parsedQs = qs.parse($querystring);

  query = R.mergeRight(query, {
    page: queryStringIntegerProp(parsedQs, 'page'),
    'kasittelija-id': queryStringIntegerProp(parsedQs, 'kasittelija-id'),
    'has-kasittelija': queryStringBooleanProp(parsedQs, 'has-kasittelija'),
    'include-kasitelty': queryStringBooleanProp(parsedQs, 'include-kasitelty')
  });

  $: pageCount = Math.ceil(R.divide(ketjutCount, pageSize));

  const nextPageCallback = nextPage =>
    (query = R.assoc('page', Maybe.fromNull(nextPage), query));

  const queryToBackendParams = query => ({
    offset: R.compose(
      R.map(R.compose(R.multiply(pageSize), R.dec)),
      R.prop('page')
    )(query),
    limit: Maybe.Some(pageSize),
    'kasittelija-id': R.prop('kasittelija-id', query),
    'include-kasitelty': R.prop('include-kasitelty', query),
    'has-kasittelija': R.compose(
      R.filter(R.not),
      R.prop('has-kasittelija')
    )(query)
  });

  const load = query => {
    overlay = true;
    R.compose(
      Future.fork(
        response => {
          const msg = i18n(
            Maybe.orSome(
              'viesti.all.messages.load-error',
              Response.localizationKey(response)
            )
          );

          flashMessageStore.add('viesti', 'error', msg);
          overlay = false;
        },
        response => {
          resources = Maybe.Some(response);
          ketjutCount = response.ketjutCount.count;
          overlay = false;
        }
      ),
      R.chain(whoami =>
        Future.parallelObject(4, {
          whoami: Future.resolve(whoami),
          ketjutCount: api.getKetjutCount(
            Kayttajat.isLaatija(whoami)
              ? {}
              : R.compose(
                  R.omit(['offset', 'limit']),
                  queryToBackendParams
                )(query)
          ),
          ketjut: api.getKetjut(
            Kayttajat.isLaatija(whoami) ? {} : queryToBackendParams(query)
          ),
          vastaanottajaryhmat: api.vastaanottajaryhmat,
          kasittelijat: api.getKasittelijat
        })
      )
    )(kayttajaApi.whoami);
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
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`viesti.all.messages.update-success`)
        );
        overlay = false;
        load(query);
      }
    ),
    R.tap(() => {
      overlay = true;
    }),
    api.putKetju(fetch)
  );

  const isSelf = R.curry((whoami, id) =>
    R.compose(Maybe.exists(R.equals(whoami.id)), Maybe.fromNull)(id)
  );

  const formatKasittelija = R.curry((kasittelijat, whoami, id) =>
    R.ifElse(
      isSelf(whoami),
      R.always(i18n('viesti.mina')),
      R.compose(
        Maybe.orSome('-'),
        R.map(kasittelija => `${kasittelija.etunimi} ${kasittelija.sukunimi}`),
        R.chain(id => Maybe.find(R.propEq('id', id), kasittelijat)),
        Maybe.fromNull
      )
    )(id)
  );

  $: load(query);

  $: R.compose(
    querystring =>
      replace(`${$location}${R.length(querystring) ? '?' + querystring : ''}`),
    qs.stringify,
    R.map(Maybe.get),
    R.filter(Maybe.isSome)
  )(query);
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { ketjut, whoami, vastaanottajaryhmat, kasittelijat }}
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
            <!--<Checkbox
              id={'checkbox.show-mine'}
              name={'checkbox.show-mine'}
              label={i18n('viesti.all.show-mine')}
              lens={R.lensProp('kasittelija-self')}
              bind:model={filters}
              disabled={false} /> -->
            <div class="w-1/4">
              <Select
                compact={true}
                label={i18n('viesti.kasittelija')}
                bind:model={query}
                lens={R.lensProp('kasittelija-id')}
                items={R.pluck('id', kasittelijat)}
                format={formatKasittelija(kasittelijat, whoami)}
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
      {/if}
      {#if ketjut.length === 0}
        <span>{i18n('viesti.all.no-messages')}</span>
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
