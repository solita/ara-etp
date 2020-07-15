<script>
  import { onMount } from 'svelte';
  import { push, location, querystring } from 'svelte-spa-router';
  import * as R from 'ramda';
  import * as qs from 'qs';
  import * as Maybe from '@Utility/maybe-utils';

  import SimpleInput from '@Component/Input/SimpleInput';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import H1 from '@Component/H/H1';
  import Select from '@Component/Select/Select';
  import Table from '@Component/Table/Table';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import { locale, _ } from '@Language/i18n';
  import * as locales from '@Language/locale-utils';
  import * as LaatijaUtils from './laatija-utils';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';
  import { currentUserStore } from '@/stores';

  import * as Future from '@Utility/future-utils';

  import { flashMessageStore } from '@/stores';

  let laatijat = Maybe.None();
  let itemsPerPage = 20;

  const hasCurrentUserAccessToField = R.ifElse(
    R.has('roles'),
    R.compose(
      KayttajaUtils.kayttajaHasAccessToResource(
        R.__,
        Maybe.get($currentUserStore)
      ),
      R.prop('roles')
    ),
    R.always(true)
  );

  const fields = R.filter(hasCurrentUserAccessToField, [
    { id: 'laatija', title: $_('laatija.laatija') },
    { id: 'puhelin', title: $_('kayttaja.puhelinnumero') },
    {
      id: 'patevyystaso',
      title: $_('laatija.patevyystaso')
    },
    {
      id: 'toteamispaivamaara',
      title: $_('laatijahaku.voimassaolo'),
      format: LaatijaUtils.formatVoimassaoloaika
    },
    { id: 'toimintaalue', title: $_('laatija.paatoimintaalue') },
    { id: 'postinumero', title: $_('laatija.postinumero') },
    { id: 'postitoimipaikka', title: $_('laatijahaku.kunta') },
    {
      id: 'yritys',
      type: 'action-with-template',
      title: $_('yritys.yritykset'),
      actionTemplate: ({ id, nimi }) => ({
        type: 'link',
        href: `#/yritys/${id}`,
        text: `${nimi}`
      })
    },
    {
      id: 'energiatodistus',
      type: 'action-with-template',
      title: $_('laatijahaku.energiatodistukset'),
      actionTemplate: ({ laatija }) => ({
        type: 'link',
        href: `#/energiatodistus/all`,
        icon: 'view_list'
      }),
      roles: [KayttajaUtils.paakayttajaRole]
    }
  ]);

  const formatLocale = R.curry((localizations, id) =>
    R.compose(
      locales.label($locale),
      R.find(R.propEq('id', id))
    )(localizations)
  );

  const findYritysById = R.curry((yritykset, id) =>
    R.find(R.propEq('id', id), yritykset)
  );

  const formatYritys = R.curry((yritykset, idt) =>
    R.compose(
      R.map(R.pick(['id', 'nimi'])),
      R.map(findYritysById(yritykset))
    )(idt)
  );

  const formatLaatija = R.curry((patevyydet, yritykset, toimintaalueet) =>
    R.compose(
      R.map(laatija =>
        R.compose(
          R.pick(
            R.compose(
              R.concat(R.__, [
                'henkilotunnus',
                'email',
                'ensitallennus',
                'laatimiskielto',
                'voimassa',
                'id'
              ]),
              R.map(R.prop('id'))
            )(fields)
          ),
          R.assoc(
            'patevyystaso',
            formatLocale(patevyydet, laatija.patevyystaso)
          ),
          R.assoc(
            'toimintaalue',
            formatLocale(toimintaalueet, laatija.toimintaalue)
          ),
          R.assoc('yritys', formatYritys(yritykset, laatija.yritys)),
          R.assoc('laatija', `${laatija.etunimi} ${laatija.sukunimi}`),
          R.assoc('energiatodistus', [{ laatija: laatija.id }])
        )(laatija)
      )
    )
  );

  const getLaatijat = R.compose(
    Future.fork(
      _ => flashMessageStore.add('Laatija', 'error', $_('errors.load-error')),
      R.compose(([result, yritykset, patevyydet, toimintaalueet]) => {
        laatijat = Maybe.Some(
          formatLaatija(patevyydet, yritykset, toimintaalueet)(result)
        );
      })
    ),
    Future.parallel(5),
    R.append(geoApi.toimintaalueet),
    R.append(laatijaApi.patevyydet),
    R.juxt([laatijaApi.getLaatijat, laatijaApi.getAllYritykset])
  );

  const onRowClick = ({ id }) => push(`#/kayttaja/${id}`);

  const nextPageCallback = nextPage => {
    model = R.assoc('page', Maybe.Some(nextPage), model);
    push(
      `${$location}?${R.compose(
        qs.stringify,
        R.map(Maybe.orSome('')),
        R.pick(['search', 'page', 'state'])
      )(model)}`
    );
  };

  const formatTila = R.compose(
    Maybe.orSome($_('validation.no-selection')),
    Maybe.map(state => $_(`laatijahaku.tilat.` + state)),
    R.when(R.complement(Maybe.isMaybe), Maybe.of)
  );

  const searchValue = R.compose(
    Maybe.orSome(''),
    R.map(
      R.compose(
        R.toLower,
        R.trim
      )
    ),
    R.prop('search')
  );

  const isMatchToSearchValue = R.curry((model, value) =>
    R.compose(
      Maybe.orSome(false),
      R.map(
        R.compose(
          R.contains(searchValue(model)),
          R.toLower,
          R.trim
        )
      ),
      Maybe.fromNull
    )(value)
  );

  const matchTransformation = R.curry(model => ({
    laatija: isMatchToSearchValue(model),
    henkilotunnus: isMatchToSearchValue(model),
    email: isMatchToSearchValue(model),
    yritys: R.compose(
      R.complement(R.isEmpty),
      R.filter(isMatchToSearchValue(model)),
      R.map(R.prop('nimi'))
    ),
    postinumero: isMatchToSearchValue(model),
    postitoimipaikka: isMatchToSearchValue(model),
    toimintaalue: isMatchToSearchValue(model),
    puhelin: isMatchToSearchValue(model)
  }));

  const laatijaSearchMatch = R.curry((model, laatija) =>
    R.compose(
      R.complement(R.isEmpty),
      R.filter(R.equals(true)),
      R.values,
      R.evolve(matchTransformation(model)),
      R.pick([
        'laatija',
        'henkilotunnus',
        'email',
        'yritys',
        'postinumero',
        'postitoimipaikka',
        'toimintaalue',
        'puhelin'
      ])
    )(laatija)
  );

  const laatijaTilaMatch = R.curry((model, laatija) =>
    R.cond([
      [R.equals(0), R.always(R.propEq('voimassa', true, laatija))],
      [R.equals(1), R.always(R.propEq('ensitallennus', false, laatija))],
      [R.equals(2), R.always(R.propEq('laatimiskielto', true, laatija))],
      [R.equals(3), R.always(R.propEq('voimassa', false, laatija))],
      [R.T, R.always(true)]
    ])(
      R.compose(
        Maybe.orSome(-1),
        R.map(parseInt),
        R.prop('state')
      )(model)
    )
  );

  const laatijaMatch = R.curry((model, laatija) =>
    R.allPass([laatijaTilaMatch(model), laatijaSearchMatch(model)])(laatija)
  );

  onMount(_ => {
    getLaatijat(fetch);
  });

  $: results = R.compose(
    Maybe.orSome([]),
    R.map(R.filter(laatijaMatch(model)))
  )(laatijat);
  $: isReusults = R.complement(R.isEmpty)(results);
  $: pageCount = Math.ceil(R.divide(R.length(results), itemsPerPage));

  $: R.compose(
    querystring => push(`${$location}?${querystring}`),
    qs.stringify,
    R.evolve({
      search: Maybe.orSome(''),
      page: Maybe.orSome(1),
      state: Maybe.orSome('')
    })
  )(model);

  $: model = R.compose(
    R.merge({ search: Maybe.None(), page: Maybe.Some(1), state: Maybe.None() }),
    R.evolve({
      search: Maybe.Some,
      page: R.compose(
        R.ifElse(Number.isInteger, Maybe.Some, R.always(Maybe.Some(1))),
        parseInt
      ),
      state: R.ifElse(R.isEmpty, Maybe.None, Maybe.Some)
    }),
    R.pick(['search', 'page', 'state'])
  )(qs.parse($querystring));
</script>

<style>

</style>

<div class="w-full mt-3">
  <H1 text={$_('laatijahaku.title')} />

  <div class="flex lg:flex-row flex-col -mx-4 my-4">
    <div class="lg:w-2/3 w-full px-4 lg:pt-10">
      <SimpleInput
        label={' '}
        wrapper={PillInputWrapper}
        search={true}
        on:input={evt => (model = R.assoc('search', Maybe.Some(evt.target.value), model))}
        viewValue={R.compose( Maybe.orSome(''), R.prop('search') )(model)} />
    </div>

    <div class="lg:w-1/3 w-full px-4 py-4">
      <Select
        label={$_('laatijahaku.tila')}
        disabled={false}
        bind:model
        lens={R.lensProp('state')}
        format={formatTila}
        parse={Maybe.Some}
        noneLabel={'laatijahaku.kaikki'}
        items={[0, 1, 2, 3]} />
    </div>
  </div>

  <div class="mt-10">
    <H1
      text={$_('laatijahaku.results', {
        values: { count: R.length(results) }
      })} />
  </div>
  {#if isReusults}
    <div class="w-full overflow-x-auto mt-4">
      <Table
        {fields}
        tablecontents={results}
        {onRowClick}
        pageNum={R.compose( Maybe.orSome(1), R.prop('page') )(model)}
        {pageCount}
        {nextPageCallback}
        {itemsPerPage} />
    </div>
  {/if}
</div>
