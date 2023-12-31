<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Formats from '@Utility/formats';
  import * as dfns from 'date-fns';

  import * as api from '@Pages/energiatodistus/energiatodistus-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';

  import { _ } from '@Language/i18n';

  import H1 from '@Component/H/H1';
  import H2 from '@Component/H/H2';
  import Overlay from '@Component/Overlay/Overlay';
  import Link from '@Component/Link/Link.svelte';
  import * as Inputs from '@Pages/energiatodistus/inputs';
  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const i18nRoot = 'energiatodistus.muutoshistoria';
  const { announceError } = announcementsForModule('Energiatodistus');

  export let params;

  let resources = Maybe.None();
  let overlay = true;

  const stateLocalizations = {
    luonnos: ['tila-id', R.equals(0)],
    allekirjoitusAloitettiin: ['tila-id', R.equals(1)],
    hylatty: ['tila-id', R.equals(3)],
    voimassaoloPaattyi: ['voimassaolo-paattymisaika', R.T],
    allekirjoitettu: ['allekirjoitusaika', R.T],
    korvattuTodistuksella: ['korvaava-energiatodistus-id', R.T],
    laskutettu: ['laskutusaika', R.T]
  };

  const stateLocalization = R.compose(
    R.head,
    R.keys,
    R.pickBy(R.__, stateLocalizations),
    event => (v, _) =>
      R.and(
        R.equals(event.k, R.head(v)),
        Maybe.fold(false, R.last(v), event['new-v'])
      )
  );

  const assocLocalizationKey = event =>
    R.assoc('localizationKey', stateLocalization(event), event);

  const formattedValue = R.curry((type, val) =>
    R.cond([
      [
        R.equals('date'),
        _ => Formats.formatTimeInstantMinutes(dfns.parseISO(val))
      ],
      [R.equals('bool'), _ => i18n(`${i18nRoot}.${val}`)],
      [R.T, _ => val]
    ])(type)
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

        announceError(msg);
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
    R.sort(R.descend(R.prop('modifytime'))),
    R.filter(R.prop('localizationKey')),
    R.map(assocLocalizationKey),
    R.filter(event => dfns.isPast(event.modifytime)),
    Maybe.orSome([]),
    R.chain(R.compose(Maybe.fromNull, R.path(['history', 'state-history'])))
  )(resources);

  $: formHistory = R.compose(
    R.sort(R.descend(R.prop('modifytime'))),
    Maybe.orSome([]),
    R.chain(R.compose(Maybe.fromNull, R.path(['history', 'form-history'])))
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
                  {#if R.and(R.equals(event.k, 'korvaava-energiatodistus-id'), event['new-v'].isSome())}
                    <Link
                      href={`#/energiatodistus/${Maybe.orSome(
                        '',
                        event['new-v']
                      )}`}
                      text={Maybe.orSome('', event['new-v'])} />
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

      <div class="overflow-x-auto my-10">
        <H2 text={i18n(`${i18nRoot}.energiatodistuslomake`)} />
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
                {i18n(`${i18nRoot}.kentta`)}
              </th>
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.ensimmainenArvo`)}
              </th>
              <th class="etp-table--th">
                {i18n(`${i18nRoot}.nykyinenArvo`)}
              </th>
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each formHistory as event}
              <tr class="etp-table--tr">
                <td class="etp-table--td">
                  {Formats.formatTimeInstantMinutes(event.modifytime)}
                </td>
                <td class="etp-table--td">
                  {event['modifiedby-fullname']}
                </td>
                <td class="etp-table--td">
                  {Inputs.propertyLabel($_, event.k)}
                </td>
                <td class="etp-table--td">
                  {event['init-v'].map(formattedValue(event.type)).orSome('-')}
                </td>
                <td class="etp-table--td">
                  {event['new-v'].map(formattedValue(event.type)).orSome('-')}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>
  </Overlay>
</div>
