<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as dfns from 'date-fns';

  import * as api from './energiatodistus-api';
  import * as kayttajaApi from '@Component/Kayttaja/kayttaja-api';

  import { flashMessageStore } from '@/stores';
  import { _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Overlay from '@Component/Overlay/Overlay';
  import Link from '@Component/Link/Link.svelte';

  const i18n = $_;
  const i18nRoot = 'energiatodistus.muutoshistoria';
  export let params;

  let resources = Maybe.None();
  let overlay = true;

  const stateLocalizations = {
    luonnosLaskentaohjelmisto: ['tila-id', R.equals(0)],
    allekirjoitusAloitettiin: ['tila-id', R.equals(1)],
    hylatty: ['tila-id', R.equals(3)],
    voimassaoloPaattyi: ['voimassaolo-paattymisaika', R.T],
    allekirjoitettu: ['allekirjoitusaika', R.T],
    korvattuTodistuksella: ['korvaava-energiatodistus-id', R.T]
  };

  const stateLocalization = R.compose(
    R.head,
    R.keys,
    R.pickBy(R.__, stateLocalizations),
    event => (v, _) => R.and(R.equals(event.k, R.head(v)), R.last(v)(event.v))
  );

  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Response.notFound(response)
            ? `${i18nRoot}.not-found`
            : Maybe.orSome(
                `${i18nRoot}.load-error`,
                Response.localizationKey(response)
              )
        );

        flashMessageStore.add('Energiatodistus', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
      },
      Future.parallelObject(2, {
        whoami: kayttajaApi.whoami,
        history: api.getEnergiatodistusHistoryById(id)
      })
    );
  };

  $: stateHistory = R.compose(
    R.reverse,
    R.filter(R.prop('localizationKey')),
    R.map(event => R.assoc('localizationKey', stateLocalization(event), event)),
    R.filter(event => dfns.isPast(event.modifytime)),
    R.map(R.over(R.lensProp('modifytime'), dfns.parseJSON)),
    Maybe.orSome([]),
    R.map(R.prop('state-history')),
    R.map(R.prop('history'))
  )(resources);

  load(params.id);
</script>

<style>
</style>

<div class="w-full mt-3">
  <H1 text={i18n(`${i18nRoot}.title`)} />
  <Overlay {overlay}>
    <div slot="content" class="mb-10">
      <div class="overflow-x-auto">
        <H2 text={i18n(`${i18nRoot}.tilamuutokset`)} />
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr">
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.ajankohta`)}
              </th>
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.kayttaja`)}
              </th>
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.tapahtuma`)}
              </th>
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.lisatietoa`)}
              </th>
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each stateHistory as event}
              <tr class="etp-table--tr">
                <td class="etp-table--td">
                  {Formats.formatTimeInstantMinutes(event.modifytime)}
                </td>
                <td class="etp-table--td">
                  {event['modifiedby-fullname']}
                </td>
                <td class="etp-table--td">
                  {$_(i18nRoot + '.' + event.localizationKey)}
                  {#if R.equals(event.k, 'korvaava-energiatodistus-id')}
                    <Link
                      href={`#/energiatodistus/${event.v}`}
                      text={event.v} />
                  {/if}
                </td>
                <td class="etp-table--td">
                  {#if R.equals(true, event['external-api'])}
                    {$_(`${i18nRoot}.external-api`)}
                  {/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  </Overlay>
</div>
