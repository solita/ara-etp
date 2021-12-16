<script>
  import * as R from 'ramda';
  import * as dfns from 'date-fns';
  import * as Parsers from '@Utility/parsers';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';

  import { replace, querystring, location } from 'svelte-spa-router';
  import * as Router from '@Component/Router/router';
  import qs from 'qs';

  import * as Toimenpiteet from '@Pages/valvonta-oikeellisuus/toimenpiteet';
  import * as Valvojat from '@Pages/valvonta/valvojat';

  import * as ETViews from '@Pages/energiatodistus/views';
  import * as Links from './links';

  import * as api from './valvonta-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as EnergiatodistusApi from '@Pages/energiatodistus/energiatodistus-api';

  import { flashMessageStore } from '@/stores';
  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Address from '@Pages/energiatodistus/address';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import Link from '@Component/Link/Link.svelte';
  import Input from '@Component/Input/Input';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.all';

  let valvontaCount = 0;
  let textCancel = () => {};

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

  let parsedQs = qs.parse($querystring);

  let query = {
    page: Maybe.None(),
    'valvoja-id': Maybe.None(),
    'include-closed': Maybe.None(),
    'has-valvoja': Maybe.None(),
    keyword: Maybe.None(),
    'toimenpidetype-id': Maybe.None()
  };

  query = R.mergeRight(query, {
    page: queryStringIntegerProp(parsedQs, 'page'),
    'valvoja-id': queryStringIntegerProp(parsedQs, 'valvoja-id'),
    'include-closed': queryStringBooleanProp(parsedQs, 'include-closed'),
    'has-valvoja': queryStringBooleanProp(parsedQs, 'has-valvoja'),
    keyword: Maybe.fromEmpty(R.prop('keyword', parsedQs)),
    'toimenpidetype-id': queryStringIntegerProp(parsedQs, 'toimenpidetype-id')
  });

  const nextPageCallback = nextPage =>
    (query = R.assoc('page', Maybe.Some(nextPage), query));

  const wrapPercent = q => `%${q}%`;

  const queryToBackendParams = query => ({
    offset: R.compose(
      R.map(R.compose(R.multiply(pageSize), R.dec)),
      R.prop('page')
    )(query),
    limit: Maybe.Some(pageSize),
    'valvoja-id': R.prop('valvoja-id', query),
    keyword: R.map(R.compose(encodeURI, wrapPercent), R.prop('keyword', query)),
    'toimenpidetype-id': R.prop('toimenpidetype-id', query),
    'include-closed': R.prop('include-closed', query),
    'has-valvoja': R.compose(R.filter(R.not), R.prop('has-valvoja'))(query)
  });

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            i18nRoot + '.messages.load-error',
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        valvontaCount = response.count;
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
        valvojat: api.valvojat,
        valvonnat: api.valvonnat(queryToBackendParams(query))
      })
    );
  }

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

  $: R.compose(
    querystring =>
      replace(`${$location}${R.length(querystring) ? '?' + querystring : ''}`),
    qs.stringify,
    R.map(Maybe.get),
    R.filter(Maybe.isSome)
  )(query);
</script>

<!-- purgecss: font-bold text-primary text-error -->

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { valvonnat, whoami, luokittelut, toimenpidetyypit, valvojat }}
      <H1 text={i18n(i18nRoot + '.title')} />
      {#if Kayttajat.isPaakayttaja(whoami)}
        <div class="flex flex-wrap items-end lg:space-y-0 space-y-4">
          <div class="w-1/4 mr-4">
            <Select
              disabled={overlay}
              compact={true}
              label={i18n(i18nRoot + '.valvoja')}
              bind:model={query}
              lens={R.lensProp('valvoja-id')}
              items={R.pluck(
                'id',
                R.filter(R.propEq('passivoitu', false), valvojat)
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
      {/if}

      <div
        class="flex flex-wrap items-end lg:space-x-4 lg:space-y-0 space-y-4 my-4">
        <div class="lg:w-1/2 w-full px-4 py-4">
          <Input
            label={i18n(i18nRoot + '.keyword-search')}
            model={query}
            lens={R.lensProp('keyword')}
            format={Maybe.orSome('')}
            parse={Parsers.optionalString}
            search={true}
            on:input={evt => {
              textCancel();
              textCancel = Future.value(keyword => {
                query = R.assoc('keyword', keyword, query);
              }, Future.after(1000, Maybe.fromEmpty(R.trim(evt.target.value))));
            }} />
        </div>
        <div class="w-1/2 lg:w-1/4">
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
                R.find(R.propEq('id', id), toimenpidetyypit)
              )}
            parse={Maybe.Some}
            allowNone={true} />
        </div>
      </div>
      {#if valvonnat.length === 0}
        <div class="my-6">{i18n(i18nRoot + '.empty')}</div>
      {:else}
        <div class="my-6 overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.valvoja')}
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
                      {Locales.labelForId(
                        $locale,
                        toimenpidetyypit
                      )(toimenpide['type-id'])}
                      {#if Toimenpiteet.isDraft(toimenpide)}
                        ({i18n(i18nRoot + '.table.draft')})
                      {/if}
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
                    {orEmpty(valvonta.energiatodistus.perustiedot.nimi)}
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

      {#if R.gt(valvontaCount, pageSize)}
        <div class="pagination">
          <Pagination
            pageNum={Maybe.orSome(1, R.prop('page', query))}
            {nextPageCallback}
            itemsPerPage={pageSize}
            itemsCount={valvontaCount} />
        </div>
      {/if}

      {#if Kayttajat.isPaakayttaja(whoami)}
        <Link
          icon={Maybe.Some('download_for_offline')}
          href="api/private/valvonta/oikeellisuus/csv/valvonta.csv"
          text={i18n(i18nRoot + '.download-all')} />
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
