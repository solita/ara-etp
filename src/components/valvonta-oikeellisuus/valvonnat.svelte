<script>
  import * as R from 'ramda';
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

  import * as ETViews from '@Component/Energiatodistus/views';
  import * as ET from '@Component/Energiatodistus/energiatodistus-utils';

  import * as api from './valvonta-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as EnergiatodistusApi from '@Component/Energiatodistus/energiatodistus-api';

  import { flashMessageStore } from '@/stores';
  import { _, locale } from '@Language/i18n';
  import * as Locales from '@Language/locale-utils';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Pagination from '@Component/Pagination/Pagination';
  import Address from '@Component/Energiatodistus/address';

  let resources = Maybe.None();
  let overlay = true;

  const pageSize = 50;
  let valvontaCount = 0;
  let page = Maybe.Some(1);

  $: page = R.compose(
    R.chain(Either.toMaybe),
    R.map(Parsers.parseInteger),
    Maybe.fromEmpty,
    R.prop('page'),
    qs.parse
  )($querystring);

  const nextPageCallback = nextPage => Router.push(`#/valvonta/oikeellisuus/all?page=${nextPage}`);

  $: {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
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
        valvontaCount = response.count;
        overlay = false;
      },
      Future.parallelObject(3, {
        whoami: kayttajaApi.whoami,
        count: R.map(R.prop('count'), api.valvontaCount),
        luokittelut: EnergiatodistusApi.luokittelutAllVersions,
        toimenpidetyypit: api.toimenpidetyypit,
        valvonnat: api.valvonnat({
          offset: R.map(R.compose(R.multiply(pageSize), R.dec), page),
          limit: Maybe.Some(pageSize)
        })
      })
    );
  }

  const orEmpty = Maybe.orSome('');
  $: kayttotarkoitusTitle = ETViews.kayttotarkoitusTitle($locale);

  const formatDeadline = R.compose(
    EM.fold('-', Formats.formatDateInstant),
    R.prop('deadline-date'));

  const toValvontaView = energiatodistus => {
    Router.push('#/valvonta/oikeellisuus/' + energiatodistus.versio + '/' +  energiatodistus.id);
  };
</script>

<style>
  .header > * {
    @apply block font-bold text-primary uppercase text-left tracking-wider bg-light text-sm;
  }
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { valvonnat, whoami, luokittelut, toimenpidetyypit }}
      <H1 text={$_('valvonta.oikeellisuus.all.title')} />
      {#if valvonnat.length === 0}
        <span>{$_('valvonta.oikeellisuus.all.empty')}</span>
      {:else}
        <div class="my-6">
          <table class="etp-table">
            <thead class="etp-table--thead">
            <tr class="etp-table--tr etp-table--tr__light">
              <th class="etp-table--th">
                {$_('valvonta.oikeellisuus.all.table.publish-time')}
              </th>
              <th class="etp-table--th">
                {$_('valvonta.oikeellisuus.all.table.type')}
              </th>
              <th class="etp-table--th">
                {$_('valvonta.oikeellisuus.all.table.deadline-date')}
              </th>

              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.tila')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.tunnus')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.e-luokka')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.versio')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.rakennuksen-nimi')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.osoite')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.ktl')}
              </th>
              <th class="etp-table--th">
                {$_('energiatodistus.haku.sarakkeet.laatija')}
              </th>
            </tr>
            </thead>
            <tbody class="etp-table--tbody">
            {#each valvonnat as {energiatodistus, ...valvonta}}
              <tr class="etp-table--tr etp-table--tr__link"
                  on:click={toValvontaView(energiatodistus)}>
                <!-- valvonta -->
                <td class="etp-table--td">
                  {Formats.formatTimeInstant(valvonta['publish-time'])}
                </td>
                <td class="etp-table--td">
                  {Locales.labelForId($locale, toimenpidetyypit)(valvonta['type-id'])}
                </td>
                <td class="etp-table--td">
                  {formatDeadline(valvonta)}
                </td>

                <!-- energiatodistus -->
                <td class="etp-table--td">
                  {$_(
                    'energiatodistus.tila.' +
                    ET.tilaKey(energiatodistus['tila-id'])
                  )}
                </td>
                <td class="etp-table--td">{energiatodistus.id}</td>
                <td class="etp-table--td">
                  {orEmpty(energiatodistus.tulokset['e-luokka'])}
                </td>
                <td class="etp-table--td">{energiatodistus.versio}</td>
                <td class="etp-table--td">
                  {orEmpty(energiatodistus.perustiedot.nimi)}
                </td>
                <td class="etp-table--td">
                  <Address
                      {energiatodistus}
                      postinumerot={luokittelut.postinumerot} />
                </td>
                <td class="etp-table--td"
                    title={kayttotarkoitusTitle(
                        luokittelut,
                        energiatodistus
                      )}>
                  {orEmpty(energiatodistus.perustiedot.kayttotarkoitus)}
                </td>
                <td class="etp-table--td">
                  {orEmpty(energiatodistus['laatija-fullname'])}
                </td>
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
