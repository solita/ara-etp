<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as api from './energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as et from './energiatodistus-utils';
  import * as dfns from 'date-fns';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';

  import {flatSchema} from '@Component/energiatodistus-haku/schema';

  import { querystring } from 'svelte-spa-router';
  import qs from 'qs';

  import { _ } from '@Language/i18n';
  import { flashMessageStore, currentUserStore } from '@/stores';
  import { push, replace } from '@Component/Router/router';

  import H1 from '@Component/H/H1';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import Link from '@Component/Link/Link';
  import Select from '@Component/Select/Select';
  import Confirm from '@Component/Confirm/Confirm';
  import EnergiatodistusHaku from '@Component/energiatodistus-haku/energiatodistus-haku';

  import * as EtHakuUtils from '@Component/energiatodistus-haku/energiatodistus-haku-utils';
  import * as EtHakuSchema from '@Component/energiatodistus-haku/schema';

  let overlay = true;
  let failure = false;
  let energiatodistukset = [];
  let schema = Maybe.None();

  let cancel = () => {};

  const toggleOverlay = value => () => (overlay = value);
  const orEmpty = Maybe.orSome('');

  const formatTila = R.compose(
    Maybe.orSome($_('validation.no-selection')),
    Maybe.map(tila => $_(`energiatodistukset.tilat.` + tila)),
    R.when(R.complement(Maybe.isMaybe), Maybe.of)
  );

  const toETView = (versio, id) => {
    push('#/energiatodistus/' + versio + '/' + id);
  };

  const removeEnergiatodistusFromList = R.curry((versio, id) =>
    R.filter(
      R.compose(
        R.not,
        R.allPass([R.propEq('versio', versio), R.propEq('id', id)])
      )
    )
  );

  const deleteEnergiatodistus = (versio, id) => {
    Future.fork(
      _ =>
        flashMessageStore.add(
          'Energiatodistus',
          'error',
          $_('energiatodistukset.messages.delete-error')
        ),
      _ => {
        energiatodistukset = removeEnergiatodistusFromList(versio, id)(
          energiatodistukset
        );

        flashMessageStore.add(
          'Energiatodistus',
          'success',
          $_('energiatodistukset.messages.delete-success')
        );
      },
      api.deleteEnergiatodistus(fetch, versio, id)
    );
  };

  const parseQuerystring = R.compose(
    R.mergeRight({
      tila: Maybe.None(),
      where: ''
    }),
    R.evolve({
      tila: Maybe.fromNull,
      where: R.compose(
        Either.orSome(''),
        w => Either.fromTry(() => JSON.parse(w))
      )
    }),
    qs.parse
  );

  $: if (!qs.parse($querystring).where) {
    replace('#/energiatodistus/all?where=[[]]');
  }

  $: parsedQuery = parseQuerystring($querystring);

  $: where = R.prop('where', parsedQuery);
  $: tila = R.prop('tila', parsedQuery);

  const queryToQuerystring = R.compose(
    api.toQueryString,
    R.pickBy(Maybe.isSome),
    R.evolve({
      where: R.compose(
        R.map(encodeURI),
        R.map(JSON.stringify),
        Maybe.fromEmpty
      )
    })
  );

  let queryAsQueryString = queryToQuerystring(parsedQuery);

  let initialQuery = true;

  $: if (initialQuery || $querystring !== '') {
    initialQuery = false;
    R.compose(
      Future.fork(
        () => {
          overlay = false;
          flashMessageStore.add(
            'Energiatodistus',
            'error',
            $_('energiatodistus.messages.load-error')
          );
        },
        response => {
          overlay = false;
          energiatodistukset = response[0];
          schema = R.compose(
            Maybe.Some,
            R.ifElse(
              KayttajaUtils.isPaakayttaja,
              R.always(EtHakuSchema.paakayttajaSchema),
              R.always(EtHakuSchema.laatijaSchema)
            )
          )(response[2]);
        }
      ),
      Future.parallel(5),
      R.tap(toggleOverlay(true)),
      R.tap(cancel),
      R.prepend(R.__, [api.laatimisvaiheet, kayttajaApi.whoami]),
      api.getEnergiatodistukset,
      queryToQuerystring,
      R.over(R.lensProp('where'), EtHakuUtils.convertWhereToQuery(flatSchema)),
      parseQuerystring
    )($querystring);
  }
</script>

<style>

</style>

<div class="w-full mt-3">
  <H1 text={$_('energiatodistukset.title')} />
  {#each Maybe.toArray(schema) as s}
    <div class="mb-10">
      <EnergiatodistusHaku {where} schema={s} />
    </div>
  {/each}
  <Overlay {overlay}>
    <div slot="content">
      <div class="lg:w-1/3 w-full mb-6">
        <Select
          label={'Tila'}
          disabled={false}
          bind:model={parsedQuery}
          lens={R.lensProp('tila')}
          format={formatTila}
          parse={Maybe.Some}
          noneLabel={'energiatodistus.kaikki'}
          items={[0, 1]} />
      </div>

      {#if R.isEmpty(energiatodistukset)}
        <p class="mb-10">
          Energiatodistuksia ei löydy annetuilla hakuehdoilla tai sinulle ei ole
          yhtään energiatodistusta.
        </p>
      {:else}
        <div class="mb-10">
          <table class="etp-table">
            <thead class="etp-table--thead">
              <tr class="etp-table--tr etp-table--tr__light">
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.tila')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.tunnus')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.ETL')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.versio')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.voimassa')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.rakennuksen-nimi')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.osoite')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.laatija')}
                </th>
                <th class="etp-table--th">
                  {$_('energiatodistus.haku.sarakkeet.toiminnot')}
                </th>
              </tr>
            </thead>
            <tbody class="etp-table--tbody">
              {#each energiatodistukset as energiatodistus}
                <tr
                  class="etp-table--tr etp-table--tr__link"
                  on:click={toETView(energiatodistus.versio, energiatodistus.id)}>
                  <td class="etp-table--td">
                    {$_('energiatodistus.tila.' + et.tilaKey(energiatodistus['tila-id']))}
                  </td>
                  <td class="etp-table--td">{energiatodistus.id}</td>
                  <td class="etp-table--td">C</td>
                  <td class="etp-table--td">{energiatodistus.versio}</td>
                  <td class="etp-table--td">
                    {R.compose( Maybe.fold('-', d =>
                        dfns.format(d, 'd.M.yyyy')
                      ), et.viimeinenVoimassaolo )(energiatodistus)}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot.nimi)}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus.perustiedot['katuosoite-fi'])}
                  </td>
                  <td class="etp-table--td">
                    {orEmpty(energiatodistus['laatija-fullname'])}
                  </td>
                  <td class="etp-table--td etp-table--td__center">
                    <Confirm
                      let:confirm
                      confirmButtonLabel={$_('confirm.button.delete')}
                      confirmMessage={$_('confirm.you-want-to-delete')}>
                      <span
                        class="material-icons"
                        on:click|stopPropagation={_ => confirm(deleteEnergiatodistus, energiatodistus.versio, energiatodistus.id)}>
                        delete
                      </span>
                    </Confirm>
                  </td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/if}
    </div>
    <div slot="overlay-content">
      <Spinner />
    </div>
  </Overlay>
  {#if Maybe.fold(false, KayttajaUtils.kayttajaHasAccessToResource([
      KayttajaUtils.laatijaRole
    ]), $currentUserStore)}
    <p class="mb-4">{$_('energiatodistus.et-lisays')}</p>
    <div class="mb-4 flex lg:flex-row flex-col">
      <div class="flex flex-row mb-4 mr-4">
        <span class="material-icons">add</span>
        &nbsp;
        <Link
          text={$_('energiatodistus.luo2018')}
          href="#/energiatodistus/2018/new" />
      </div>
      <div class="flex flex-row mb-4 mr-4">
        <span class="material-icons">add</span>
        &nbsp;
        <Link
          text={$_('energiatodistus.luo2013')}
          href="#/energiatodistus/2013/new" />
      </div>
    </div>
  {/if}
  <div class="flex flew-row mb-4 mr-4">
    <span class="material-icons">attachment</span>
    &nbsp;
    <Link
      text={$_('energiatodistus.lataa-xlsx')}
      href={'/api/private/energiatodistukset/xlsx/energiatodistukset.xlsx' + queryAsQueryString} />
  </div>
</div>
