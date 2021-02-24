<script>
  import { push, replace, location, querystring } from 'svelte-spa-router';
  import * as R from 'ramda';
  import * as qs from 'qs';
  import * as Maybe from '@Utility/maybe-utils';

  import SimpleInput from '@Component/Input/SimpleInput';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import H1 from '@Component/H/H1';
  import Select from '@Component/Select/Select';
  import Pagination from '@Component/Pagination/Pagination';
  import Pgn from '@Component/Pagination/pgn';
  import Table from '@Component/Table/Table';
  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import * as yritysApi from '@Component/Yritys/yritys-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import { locale, _ } from '@Language/i18n';
  import * as locales from '@Language/locale-utils';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import { currentUserStore } from '@/stores';

  import * as Future from '@Utility/future-utils';
  import * as formats from '@Utility/formats';

  import { flashMessageStore } from '@/stores';

  let laatijat = Maybe.None();
  let kayttaja = Maybe.None();
  let pageSize = 3;

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
      format: formats.formatPatevyydenVoimassaoloaika
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
        href: `#/energiatodistus/all?where=[[["=","laatija-id",${laatija}]]]`,
        icon: 'view_list'
      }),
      roles: [KayttajaUtils.paakayttajaRole]
    }
  ]);

  const formatLocale = R.curry((localizations, id) =>
    R.compose(locales.label($locale), R.find(R.propEq('id', id)))(localizations)
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

  const formatLaatijaa = R.curry(
    (patevyydet, yritykset, toimintaalueet, laatija) =>
      R.evolve(
        {
          login: Maybe.fromNull,
          patevyystaso: formatLocale(patevyydet),
          toimintaalue: R.compose(
            Maybe.orSome('-'),
            R.map(formatLocale(toimintaalueet)),
            Maybe.fromNull
          ),
          yritys: formatYritys(yritykset)
        },
        laatija
      )
  );

  Future.fork(
    _ => flashMessageStore.add('Laatija', 'error', $_('errors.load-error')),
    ({ laatijatResponse, yritykset, patevyydet, toimintaalueet, whoami }) => {
      laatijat = R.compose(
        Maybe.Some,
        R.map(formatLaatijaa(patevyydet, yritykset, toimintaalueet))
      )(laatijatResponse);
      kayttaja = Maybe.Some(whoami);
    },
    Future.parallelObject(5, {
      laatijatResponse: laatijaApi.laatijat,
      yritykset: yritysApi.getAllYritykset,
      patevyydet: laatijaApi.patevyydet,
      toimintaalueet: geoApi.toimintaalueet,
      whoami: kayttajaApi.whoami
    })
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
    R.map(R.compose(R.toLower, R.trim)),
    R.prop('search')
  );

  const isMatchToSearchValue = R.curry((model, value) =>
    R.compose(
      Maybe.orSome(false),
      R.map(R.compose(R.contains(searchValue(model)), R.toLower, R.trim)),
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
      [
        R.equals(0),
        R.always(
          R.allPass([
            R.propEq('voimassa', true),
            R.propEq('laatimiskielto', false)
          ])(laatija)
        )
      ],
      [R.equals(1), R.always(R.propSatisfies(Maybe.isNone, 'login', laatija))],
      [R.equals(2), R.always(R.propEq('laatimiskielto', true, laatija))],
      [R.equals(3), R.always(R.propEq('voimassa', false, laatija))],
      [R.T, R.always(true)]
    ])(R.compose(Maybe.orSome(-1), R.map(parseInt), R.prop('state'))(model))
  );

  const laatijaMatch = R.curry((model, laatija) =>
    R.allPass([laatijaTilaMatch(model), laatijaSearchMatch(model)])(laatija)
  );

  $: results = R.compose(
    Maybe.orSome([]),
    R.map(R.filter(laatijaMatch(model)))
  )(laatijat);

  $: R.compose(
    querystring => replace(`${$location}?${querystring}`),
    qs.stringify,
    R.evolve({
      search: Maybe.orSome(''),
      page: Maybe.orSome(0),
      state: Maybe.orSome('')
    })
  )(model);

  $: model = R.compose(
    R.merge({ search: Maybe.None(), page: Maybe.Some(0), state: Maybe.None() }),
    R.evolve({
      search: Maybe.Some,
      page: R.compose(
        R.ifElse(Number.isInteger, Maybe.Some, R.always(Maybe.Some(0))),
        parseInt
      ),
      state: R.ifElse(R.isEmpty, Maybe.None, Maybe.Some)
    }),
    R.pick(['search', 'page', 'state'])
  )(qs.parse($querystring));

  let cancel = () => {};

  const urlForPage = R.curry((query, page) =>
    R.compose(
      R.join('&'),
      R.map(R.join('=')),
      R.toPairs,
      R.mergeLeft({ page }),
      R.evolve({ state: Maybe.orSome(''), search: Maybe.orSome('') })
    )(query)
  );

  $: parsedQuery = R.compose(
    R.evolve({
      search: Maybe.fromEmpty,
      page: R.compose(
        R.filter(i => !isNaN(i)),
        R.map(i => parseInt(i, 10)),
        Maybe.fromEmpty
      ),
      state: Maybe.fromEmpty
    }),
    qs.parse
  )($querystring);
</script>

<div class="w-full mt-3">
  <H1 text={$_('laatijahaku.title')} />

  <div class="flex lg:flex-row flex-col -mx-4 my-4">
    <div class="lg:w-2/3 w-full px-4 lg:pt-10">
      <SimpleInput
        label={' '}
        wrapper={PillInputWrapper}
        search={true}
        on:input={evt => {
          cancel = R.compose(
            Future.value(val => {
              model = R.assoc('search', Maybe.Some(val), model);
            }),
            Future.after(200),
            R.tap(cancel)
          )(evt.target.value);
        }}
        viewValue={R.compose(Maybe.orSome(''), R.prop('search'))(model)} />
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
  {#each R.compose(Maybe.toArray, R.sequence(Maybe.of))([
    Maybe.Some(results),
    kayttaja
  ]) as [laatijat, kayttaja]}
    <Pgn
      items={laatijat}
      page={Maybe.orSome(0, parsedQuery.page)}
      urlFn={urlForPage(parsedQuery)}
      baseUrl={'#/laatija/all?'}
      let:pageItems>
      <table class="etp-table">
        <thead class="etp-table--thead">
          <th class="etp-table--th">{$_('laatija.laatija')}</th>
          <th class="etp-table--th">{$_('kayttaja.puhelinnumero')}</th>
          <th class="etp-table--th">{$_('laatija.patevyystaso')}</th>
          <th class="etp-table--th">{$_('laatijahaku.voimassaolo')}</th>
          <th class="etp-table--th">{$_('laatija.paatoimintaalue')}</th>
          <th class="etp-table--th">{$_('laatija.postinumero')}</th>
          <th class="etp-table--th">{$_('laatijahaku.kunta')}</th>
          <th class="etp-table--th">{$_('yritys.yritykset')}</th>
          {#if Kayttajat.isPaakayttaja(kayttaja)}
            <th class="etp-table--th"
              >{$_('laatijahaku.energiatodistukset')}</th>
          {/if}
        </thead>
        <tbody class="etp-table--tbody">
          {#each pageItems as laatija}
            <tr class="etp-table--tr">
              <td class="etp-table--td"
                >{`${laatija.etunimi} ${laatija.sukunimi}`}</td>
              <td class="etp-table--td">{laatija.puhelin}</td>
              <td class="etp-table--td">{laatija.patevyystaso}</td>
              <td class="etp-table--td"
                >{formats.formatPatevyydenVoimassaoloaika(
                  laatija.toteamispaivamaara
                )}</td>
              <td class="etp-table--td">{laatija.toimintaalue}</td>
              <td class="etp-table--td">{laatija.postinumero}</td>
              <td class="etp-table--td">{laatija.postitoimipaikka}</td>
              <td class="etp-table--td">
                {#each laatija.yritys as { id, nimi }}
                  <a
                    class="text-primary hover:underline"
                    href={`#/yritys/${id}`}>
                    {nimi}
                  </a>
                {/each}
              </td>
              {#if Kayttajat.isPaakayttaja(kayttaja)}
                <td class="etp-table--td">
                  <a
                    class="font-icon text-2xl text-primary hover:underline"
                    href={`#/energiatodistus/all?where=[[["=","laatija-id",${laatija.id}]]]`}>
                    view_list
                  </a>
                </td>
              {/if}
            </tr>
          {/each}
        </tbody>
      </table>
    </Pgn>
  {/each}
  <!-- {#if isReusults}
    <div class="w-full overflow-x-auto mt-4">
      
        
        <Table
        {fields}
        tablecontents={results}
        {onRowClick}
        pageNum={R.compose(Maybe.orSome(1), R.prop('page'))(model)}
        {nextPageCallback}
        {itemsPerPage} /> 
    </div>
  {/if} -->
</div>
