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

  import { replace, querystring, location } from 'svelte-spa-router';
  import * as Router from '@Component/Router/router';
  import qs from 'qs';

  import * as Links from './links';
  import * as Valvojat from '@Pages/valvonta/valvojat';
  import * as Kayttajat from '@Utility/kayttajat';

  import * as api from './valvonta-api';
  import * as osapuolet from './osapuolet';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as geoApi from '@Utility/api/geo-api';

  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Select from '@Component/Select/Select';
  import Select2 from '@Component/Select/Select2';
  import Link from '@Component/Link/Link';
  import Address from '@Component/address/building-address';
  import Input from '@Component/Input/Input';
  import * as ValvontaApi from '@Pages/valvonta-kaytto/valvonta-api';
  import * as Selects from '@Component/Select/select-util';
  import { announcementsForModule } from '@Utility/announce';
  import { announcePolitely } from '@Utility/aria-live';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.all';
  const { announceError } = announcementsForModule('valvonta-kaytto');

  const announceSearchResults = valvonnatCount => {
    announcePolitely(
      i18n(i18nRoot + '.screen-reader.search-results', {
        values: { count: valvonnatCount }
      })
    );
  };

  let textCancel = () => {};

  let valvontaCount = 0;
  let pageCount;
  $: pageCount = Math.ceil(R.divide(valvontaCount, pageSize));

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
    'only-uhkasakkoprosessi': Maybe.None(),
    keyword: Maybe.None(),
    'toimenpidetype-id': Maybe.None(),
    'asiakirjapohja-id': Maybe.None()
  };

  query = R.mergeRight(query, {
    page: queryStringIntegerProp(parsedQs, 'page'),
    'valvoja-id': queryStringIntegerProp(parsedQs, 'valvoja-id'),
    'include-closed': queryStringBooleanProp(parsedQs, 'include-closed'),
    'has-valvoja': queryStringBooleanProp(parsedQs, 'has-valvoja'),
    'only-uhkasakkoprosessi': queryStringBooleanProp(
      parsedQs,
      'only-uhkasakkoprosessi'
    ),
    keyword: Maybe.fromEmpty(R.prop('keyword', parsedQs)),
    'toimenpidetype-id': queryStringIntegerProp(parsedQs, 'toimenpidetype-id'),
    'asiakirjapohja-id': queryStringIntegerProp(parsedQs, 'asiakirjapohja-id')
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
    keyword: R.map(wrapPercent, R.prop('keyword', query)),
    'toimenpidetype-id': R.prop('toimenpidetype-id', query),
    'asiakirjapohja-id': R.prop('asiakirjapohja-id', query),
    'include-closed': R.prop('include-closed', query),
    'only-uhkasakkoprosessi': R.prop('only-uhkasakkoprosessi', query),
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

        announceError(msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        announceSearchResults(response.count);
        valvontaCount = response.count;
        overlay = false;
      },
      Future.parallelObject(6, {
        whoami: kayttajaApi.whoami,
        count: R.compose(
          R.map(R.prop('count')),
          api.valvontaCount,
          R.omit(['offset', 'limit']),
          queryToBackendParams
        )(query),
        toimenpidetyypit: api.toimenpidetyypit,
        postinumerot: geoApi.postinumerot,
        valvojat: api.valvojat,
        valvonnat: api.valvonnat(queryToBackendParams(query)),
        templates: ValvontaApi.templates
      })
    );
  }

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

  const toValvontaView = valvonta => {
    Router.push(Links.valvonta(valvonta));
  };

  const formatHenkiloOmistajat = R.compose(
    R.map(henkilo => henkilo.etunimi + ' ' + henkilo.sukunimi),
    R.filter(osapuolet.isOmistaja),
    R.prop('henkilot')
  );

  const formatYritysOmistajat = R.compose(
    R.map(R.prop('nimi')),
    R.filter(osapuolet.isOmistaja),
    R.prop('yritykset')
  );

  const diaarinumero = R.compose(
    R.chain(R.prop('diaarinumero')),
    R.prop('lastToimenpide')
  );

  $: R.compose(
    querystring =>
      replace(`${$location}${R.length(querystring) ? '?' + querystring : ''}`),
    qs.stringify,
    R.map(Maybe.get),
    R.filter(Maybe.isSome)
  )(query);

  const getTemplateName = templates =>
    R.compose(Locales.labelForId($locale))(templates);
</script>

<style>
  .pagination:not(empty) {
    @apply mt-4;
  }
</style>

<!-- purgecss: font-bold text-primary text-error -->

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <div class="flex justify-between">
      <H1 text={i18n(i18nRoot + '.title')} />
      <div class="flex mb-auto">
        <Link
          href="/#/valvonta/kaytto/new"
          icon={Maybe.Some('add_circle_outline')}
          text={i18n(i18nRoot + '.new-kohde')} />
      </div>
    </div>
    {#each Maybe.toArray(resources) as { valvonnat, whoami, toimenpidetyypit, valvojat, postinumerot, templates }}
      <div class="flex flex-wrap items-end md:space-y-0 space-y-4">
        <div class="w-1/4 mr-4">
          <Select
            disabled={overlay}
            compact={true}
            label={i18n('valvonta.valvoja')}
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

        <div class="flex flex-grow md:justify-center">
          <Checkbox
            disabled={overlay}
            label={i18n(`${i18nRoot}.only-uhkasakkoprosessi`)}
            bind:model={query}
            lens={R.lensProp('only-uhkasakkoprosessi')}
            format={Maybe.orSome(false)}
            parse={Maybe.Some} />
        </div>

        <div class="flex flex-grow justify-end">
          <Checkbox
            disabled={overlay}
            label={i18n(`${i18nRoot}.include-closed`)}
            bind:model={query}
            lens={R.lensProp('include-closed')}
            format={Maybe.orSome(false)}
            parse={Maybe.Some} />
        </div>
      </div>
      <div class="grid grid-cols-4 items-end gap-4 md:space-y-0 space-y-4 my-4">
        <div class="md:col-span-2 col-span-4">
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
        <div class="md:col-span-1 col-span-4">
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
        <div class="md:col-span-1 col-span-4">
          <Select2
            label={i18n('valvonta.kaytto.all.last-template')}
            bind:model={query}
            lens={R.lensProp('asiakirjapohja-id')}
            modelToItem={Maybe.fold(
              Maybe.None(),
              Maybe.findById(R.__, templates)
            )}
            itemToModel={Maybe.fold(Maybe.None(), it => Maybe.Some(it.id))}
            format={Maybe.fold(
              i18n('validation.no-selection'),
              Locales.label($locale)
            )}
            items={Selects.addNoSelection(templates)} />
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
                  {i18n('valvonta.valvoja')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.diaarinumero')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.last-toimenpide')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.last-template')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.deadline-date')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.rakennustunnus')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.osoite')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.omistajat')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.energiatodistus')}
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each valvonnat as valvonta}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toValvontaView(valvonta)}>
                  <!-- Valvonta -->
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
                  <!-- Diaarinumero -->
                  <td class="etp-table--td">
                    {Maybe.orSome('-', diaarinumero(valvonta))}
                  </td>
                  <!-- Edellinen tapahtuma -->
                  <td class="etp-table--td">
                    {Maybe.fold(
                      i18n(i18nRoot + '.last-toimenpide-none'),
                      toimenpide =>
                        Locales.labelForId(
                          $locale,
                          toimenpidetyypit
                        )(toimenpide['type-id']),
                      valvonta.lastToimenpide
                    )}
                  </td>
                  <!-- Asiakirjapohja -->
                  <td class="etp-table--td">
                    {Maybe.fold(
                      '-',
                      toimenpide =>
                        Maybe.fold(
                          '',
                          getTemplateName(templates),
                          toimenpide['template-id']
                        ),
                      valvonta.lastToimenpide
                    )}
                  </td>
                  <!-- Määräaika -->
                  <td
                    class="etp-table--td"
                    class:text-error={Maybe.fold(
                      false,
                      isPastDeadline,
                      valvonta.lastToimenpide
                    )}
                    class:text-primary={Maybe.fold(
                      false,
                      isTodayDeadline,
                      valvonta.lastToimenpide
                    )}
                    class:font-bold={Maybe.fold(
                      false,
                      R.anyPass([isTodayDeadline, isPastDeadline]),
                      valvonta.lastToimenpide
                    )}>
                    {Maybe.fold(
                      '-',
                      toimenpide => formatDeadline(toimenpide),
                      valvonta.lastToimenpide
                    )}
                  </td>
                  <!-- Rakennustunnus -->
                  <td class="etp-table--td">
                    {Maybe.orSome('-', valvonta.rakennustunnus)}
                  </td>
                  <!-- Osoite -->
                  <td class="etp-table--td">
                    <Address
                      {postinumerot}
                      katuosoite={Maybe.Some(valvonta.katuosoite)}
                      postinumero={valvonta.postinumero} />
                  </td>
                  <!-- Omistajat -->
                  <td class="etp-table--td">
                    {R.join(
                      ', ',
                      R.concat(
                        formatHenkiloOmistajat(valvonta),
                        formatYritysOmistajat(valvonta)
                      )
                    )}
                  </td>
                  <!-- Energiatodistus -->
                  <td class="etp-table--td" on:click|stopPropagation>
                    {#each Maybe.toArray(valvonta.energiatodistus) as energiatodistus}
                      <Link
                        href={`#/energiatodistus/${energiatodistus.id}`}
                        text={`ET ${energiatodistus.id}`} />
                    {/each}
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>

          {#if R.gt(pageCount, 1)}
            <div class="pagination">
              <Pagination
                {pageCount}
                pageNum={Maybe.orSome(1, R.prop('page', query))}
                {nextPageCallback}
                itemsPerPage={pageSize}
                itemsCount={valvontaCount} />
            </div>
          {/if}
        </div>
      {/if}

      {#if Kayttajat.isPaakayttaja(whoami)}
        <Link
          icon={Maybe.Some('download_for_offline')}
          href="api/private/valvonta/kaytto/csv/valvonta.csv"
          text={i18n(i18nRoot + '.download-all')} />
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
