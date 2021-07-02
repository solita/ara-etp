<script>
  import { replace, location, querystring } from 'svelte-spa-router';
  import * as R from 'ramda';
  import * as qs from 'qs';
  import * as Maybe from '@Utility/maybe-utils';

  import Input from '@Component/Input/Input';
  import PillInputWrapper from '@Component/Input/PillInputWrapper';
  import H1 from '@Component/H/H1';
  import Select from '@Component/Select/Select';
  import Pagination from '@Component/Pagination/items-pagination';
  import * as laatijaApi from '@Component/Laatija/laatija-api';
  import * as yritysApi from '@Pages/yritys/yritys-api';
  import * as geoApi from '@Component/Geo/geo-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import { locale, _ } from '@Language/i18n';
  import * as locales from '@Language/locale-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import Linkrow from '@Component/linkrow/linkrow';

  import * as Future from '@Utility/future-utils';
  import * as formats from '@Utility/formats';

  import { flashMessageStore } from '@/stores';

  let laatijat = Maybe.None();
  let kayttaja = Maybe.None();

  const formatYritys = R.curry((yritykset, ids) =>
    R.compose(
      R.map(Maybe.get),
      R.filter(Maybe.isSome),
      R.map(
        R.compose(
          R.map(R.pick(['id', 'nimi'])),
          Maybe.findById(R.__, yritykset)
        )
      )
    )(ids)
  );

  const formatLocale = R.curry((localizations, id) =>
    R.compose(
      Maybe.orSome(id),
      R.map(locales.label($locale)),
      Maybe.findById(id)
    )(localizations)
  );

  const formatLaatija = R.curry(
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
    response => {
      const msg = Response.notFound(response)
        ? $_('laatija.messages.not-found')
        : $_(
            Maybe.orSome('laatija.messages.load-error'),
            Response.localizationKey(response)
          );
      flashMessageStore.add('Laatija', 'error', msg);
    },
    ({ laatijatResponse, yritykset, patevyydet, toimintaalueet, whoami }) => {
      laatijat = R.compose(
        Maybe.Some,
        R.map(formatLaatija(patevyydet, yritykset, toimintaalueet))
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

  const formatTila = R.compose(
    Maybe.orSome($_('validation.no-selection')),
    Maybe.map(state => $_(`laatijahaku.tilat.` + state)),
    R.when(R.complement(Maybe.isMaybe), Maybe.of)
  );

  const matchSearch = R.curry((search, laatija) =>
    R.compose(
      R.any(R.includes(R.compose(R.toLower, R.trim)(search))),
      R.map(R.toLower),
      R.flatten,
      R.values,
      R.pick([
        'etunimi',
        'sukunimi',
        'henkilotunnus',
        'email',
        'yritys',
        'postinumero',
        'postitoimipaikka',
        'toimintaalue',
        'puhelin'
      ]),
      R.over(R.lensProp('yritys'), R.pluck('nimi'))
    )(laatija)
  );

  const matchTila = R.curry((state, laatija) =>
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
    ])(state)
  );

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

  $: model = R.compose(
    R.mergeRight({
      search: Maybe.None(),
      page: Maybe.Some(0),
      state: Maybe.None()
    }),
    R.evolve({
      search: Maybe.Some,
      page: R.compose(
        R.filter(i => !isNaN(i)),
        R.map(i => parseInt(i, 10)),
        Maybe.fromEmpty
      ),
      state: R.compose(
        R.filter(i => !isNaN(i)),
        R.map(i => parseInt(i, 10)),
        Maybe.fromEmpty
      )
    }),
    R.pick(['search', 'page', 'state'])
  )(qs.parse($querystring));

  $: R.compose(
    querystring => replace(`${$location}?${querystring}`),
    qs.stringify,
    R.evolve({
      search: Maybe.orSome(''),
      page: Maybe.orSome(0),
      state: Maybe.orSome('')
    })
  )(model);

  $: results = R.compose(
    Maybe.orSome([]),
    R.map(
      R.filter(
        R.allPass([
          matchTila(Maybe.orSome(-1, model.state)),
          matchSearch(Maybe.orSome('', model.search))
        ])
      )
    )
  )(laatijat);
</script>

<div class="w-full mt-3">
  <H1 text={$_('laatijahaku.title')} />

  <div class="flex lg:flex-row flex-col -mx-4 my-4">
    <div class="lg:w-2/3 w-full px-4 lg:pt-10">
      <Input
        model={Maybe.orSome('', model.search)}
        inputComponentWrapper={PillInputWrapper}
        search={true}
        on:input={evt => {
          cancel = R.compose(
            Future.value(val => {
              model = R.mergeRight(model, {
                search: Maybe.Some(val),
                page: Maybe.Some(0)
              });
            }),
            Future.after(200),
            R.tap(cancel)
          )(evt.target.value);
        }} />
    </div>

    <div class="lg:w-1/3 w-full px-4 py-4">
      <Select
        label={$_('laatijahaku.tila')}
        disabled={false}
        model={model.state}
        lens={R.identity}
        format={formatTila}
        parse={R.identity}
        inputValueParse={Maybe.orSome('')}
        noneLabel={'laatijahaku.kaikki'}
        items={R.map(Maybe.Some, [0, 1, 2, 3])}
        on:change={evt =>
          (model = R.mergeRight(model, {
            state: Maybe.Some(evt.target.value),
            page: Maybe.Some(0)
          }))} />
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
    <Pagination
      items={laatijat}
      page={Maybe.orSome(0, model.page)}
      pageSize={20}
      urlFn={urlForPage(model)}
      baseUrl={'#/laatija/all?'}
      let:pageItems>
      <div class="overflow-x-auto">
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
            {#if Kayttajat.isPaakayttajaOrLaskuttaja(kayttaja)}
              <th class="etp-table--th"
                >{$_('laatijahaku.energiatodistukset')}</th>
            {/if}
          </thead>
          <tbody class="etp-table--tbody">
            {#each pageItems as laatija}
              <tr data-cy="laatija-row" class="etp-table--tr">
                <Linkrow
                  contents={[
                    `${laatija.etunimi} ${laatija.sukunimi}`,
                    laatija.puhelin,
                    laatija.patevyystaso,
                    formats.formatPatevyydenVoimassaoloaika(
                      laatija.toteamispaivamaara
                    ),
                    laatija.toimintaalue,
                    laatija.postinumero,
                    laatija.postitoimipaikka
                  ]}
                  href={`#/kayttaja/${laatija.id}`} />
                <td class="etp-table--td">
                  {#each laatija.yritys as { id, nimi }}
                    <a
                      class="text-primary hover:underline"
                      href={`#/yritys/${id}`}>
                      {nimi}
                    </a>
                  {/each}
                </td>
                {#if Kayttajat.isPaakayttajaOrLaskuttaja(kayttaja)}
                  <td class="etp-table--td">
                    <a
                      class="font-icon text-2xl text-primary hover:underline"
                      href={`#/energiatodistus/all?where=[[["=","energiatodistus.laatija-id",${laatija.id}]]]`}>
                      view_list
                    </a>
                  </td>
                {/if}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </Pagination>
  {/each}
</div>
