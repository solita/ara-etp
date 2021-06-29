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

  import { querystring } from 'svelte-spa-router';
  import * as Router from '@Component/Router/router';
  import qs from 'qs';

  import * as Links from './links';

  import * as api from './valvonta-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Checkbox from '@Component/Checkbox/Checkbox';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.all';

  let valvontaCount = 0;
  let onlyOwnValvonnat = false;

  $: page = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseInteger),
    Maybe.fromEmpty,
    R.prop('page'),
    qs.parse
  )($querystring);

  const nextPageCallback = nextPage =>
    Router.push(`#/valvonta/kaytto/all?page=${nextPage}`);

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
        flashMessageStore.add('valvonta-kaytto', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        valvontaCount = response.count;
        overlay = false;
      },
      Future.parallelObject(3, {
        whoami: kayttajaApi.whoami,
        toimenpidetyypit: api.toimenpidetyypit,
        count: R.map(R.prop('count'), api.valvontaCount),
        valvonnat: api.valvonnat({
          offset: R.map(R.compose(R.multiply(pageSize), R.dec), page),
          limit: Maybe.Some(pageSize),
          own: Maybe.Some(onlyOwnValvonnat)
        }),
        valvojat: api.valvojat
      })
    );
  }

  const orEmpty = Maybe.orSome('');

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

  const isSelf = R.curry((whoami, id) =>
    R.compose(Maybe.exists(R.equals(whoami.id)), Maybe.fromNull)(id)
  );

  const formatValvoja = R.curry((valvojat, whoami, id) =>
    R.compose(
      Maybe.orSome('-'),
      R.map(valvoja => `${valvoja.etunimi} ${valvoja.sukunimi}`),
      R.chain(id => Maybe.find(R.propEq('id', id), valvojat)),
      Maybe.fromNull
    )(id)
  );
</script>

<!-- purgecss: font-bold text-primary text-error -->

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text={i18n(i18nRoot + '.title')} />
    {#each Maybe.toArray(resources) as { valvonnat, whoami, toimenpidetyypit, valvojat }}
      <Checkbox
        disabled={overlay}
        label={i18n(i18nRoot + '.only-own')}
        bind:model={onlyOwnValvonnat} />
      {#if valvonnat.length === 0}
        <span>{i18n(i18nRoot + '.empty')}</span>
      {:else}
        <div class="my-6 overflow-x-auto">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.id')}
                </th>
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
                  {i18n(i18nRoot + '.table.tunnus')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.osoite')}
                </th>
                <th class="etp-table--th">
                  {i18n(i18nRoot + '.table.parties')}
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each valvonnat as valvonta}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toValvontaView(valvonta)}>
                  <td class="etp-table--td">
                    {valvonta.id}
                  </td>
                  <td
                    class="etp-table--td"
                    class:font-bold={isSelf(whoami, valvonta['valvoja-id'])}
                    class:text-primary={isSelf(whoami, valvonta['valvoja-id'])}
                    >{R.ifElse(
                      isSelf(whoami),
                      R.always(i18n('valvonta.self')),
                      formatValvoja(valvojat, whoami)
                    )(valvonta['valvoja-id'])}</td>
                  {#each Maybe.toArray(valvonta.lastToimenpide) as toimenpide}
                    <td class="etp-table--td">
                      {Locales.labelForId(
                        $locale,
                        toimenpidetyypit
                      )(toimenpide['type-id'])}
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
                    <td class="etp-table--td">Tarkastettava</td>
                    <td class="etp-table--td">-</td>
                  {/if}

                  <td class="etp-table--td" />
                  <td class="etp-table--td" />
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    {/each}
    {#if R.gt(valvontaCount, pageSize)}
      <div class="pagination">
        <Pagination
          pageNum={page.orSome(1)}
          {nextPageCallback}
          itemsPerPage={pageSize}
          itemsCount={valvontaCount} />
      </div>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
