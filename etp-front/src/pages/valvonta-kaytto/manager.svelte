<script>
  import * as R from 'ramda';
  import { isValid } from '@Utility/classification';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Valvojat from '@Pages/valvonta/valvojat';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';

  import NewToimenpideDialog from './new-toimenpide-dialog.svelte';
  import NoteDialog from './note-dialog.svelte';

  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Select from '@Component/Select/Select.svelte';
  import H2 from '@Component/H/H2.svelte';
  import * as Osapuolet from '@Pages/valvonta-kaytto/osapuolet';
  import ToimenpideSelectItem from './primary-toimenpide/ToimenpideSelectItem.svelte';
  import PrimaryToimenpideContextProvider from './primary-toimenpide/PrimaryToimenpideContextProvider.svelte';

  export let valvojat;
  export let valvonta;
  export let roolit;
  export let toimitustavat;
  export let henkilot;
  export let yritykset;
  export let toimenpiteet;
  export let toimenpidetyypit = [];
  export let templatesByType;

  export let hallintoOikeudet;
  export let karajaoikeudet;
  export let johtaja;
  export let whoami;

  export let saveValvonta;
  export let reload;
  const i18n = $_;

  let newToimenpide = Maybe.None();
  let newNote = Maybe.None();

  let toimenpideTyyppi = Maybe.None();

  const toimenpideTypeSpecificDefaultValues = toimenpideTypeId => {
    if (
      Toimenpiteet.isDecisionOrderActualDecision({
        'type-id': toimenpideTypeId
      })
    ) {
      return {
        fine: Toimenpiteet.findFineFromToimenpiteet(
          Toimenpiteet.isDecisionOrderHearingLetter,
          toimenpiteet
        ),
        departmentHeadName: johtaja['department-head-name'],
        departmentHeadTitleFi: johtaja['department-head-title-fi'],
        departmentHeadTitleSv: johtaja['department-head-title-sv'],
        osapuolis: R.filter(Osapuolet.isOmistaja, R.concat(henkilot, yritykset))
      };
    } else if (
      Toimenpiteet.isNoticeBailiff({
        'type-id': toimenpideTypeId
      })
    ) {
      return {
        osapuolis: R.filter(Osapuolet.isOmistaja, R.concat(henkilot, yritykset))
      };
    } else if (
      Toimenpiteet.isPenaltyDecisionHearingLetter({
        'type-id': toimenpideTypeId
      })
    ) {
      return {
        fine: Toimenpiteet.findFineFromToimenpiteet(
          Toimenpiteet.isDecisionOrderActualDecision,
          toimenpiteet
        ),
        osapuolis: R.filter(Osapuolet.isOmistaja, R.concat(henkilot, yritykset))
      };
    } else if (
      Toimenpiteet.isPenaltyDecisionActualDecision({
        'type-id': toimenpideTypeId
      })
    ) {
      return {
        fine: Toimenpiteet.findFineFromToimenpiteet(
          Toimenpiteet.isPenaltyDecisionHearingLetter,
          toimenpiteet
        ),
        departmentHeadName: johtaja['department-head-name'],
        departmentHeadTitleFi: johtaja['department-head-title-fi'],
        departmentHeadTitleSv: johtaja['department-head-title-sv'],
        osapuolis: R.filter(
          Osapuolet.isOmistaja,
          R.concat(henkilot, yritykset)
        ),
        defaultStatementFi: i18n(
          'valvonta.kaytto.toimenpide.penalty-decision-actual-decision.default-statement-fi'
        ),
        defaultStatementSv: i18n(
          'valvonta.kaytto.toimenpide.penalty-decision-actual-decision.default-statement-sv'
        )
      };
    } else {
      return undefined;
    }
  };

  const openNewToimenpide = type => {
    newToimenpide = Maybe.Some(
      Toimenpiteet.emptyToimenpide(
        type,
        templatesByType,
        toimenpideTypeSpecificDefaultValues(type)
      )
    );
  };

  const openNewNote = _ => {
    newNote = Maybe.Some({ description: '' });
  };

  const isAuditCase = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isAuditCase(R.last(toimenpiteet));

  const isClosed = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isCloseCase(R.last(toimenpiteet));

  const isManuallyDeliverable = R.compose(
    Toimenpiteet.isToimenpideOfGivenTypes,
    Toimenpiteet.manuallyDeliverableToimenpideTypes
  )(toimenpidetyypit);

  const isCommentingAllowed = R.compose(
    Toimenpiteet.isToimenpideOfGivenTypes,
    Toimenpiteet.toimenpideTypesThatAllowComments
  )(toimenpidetyypit);

  const saveKasittelija = id =>
    saveValvonta(R.assoc('valvoja-id', id, valvonta));

  const load = _ => {
    newToimenpide = Maybe.None();
    newNote = Maybe.None();
    toimenpideTyyppi = Maybe.None();
    reload();
  };
</script>

<style type="text/postcss">
  .warning-label {
    @apply font-bold mb-5;
  }
</style>

<!--  According to ARA's process, all osapuolet receive information for the order toimenpidetype
      and only the owners for the other toimenpidetypes -->
{#each Maybe.toArray(newToimenpide) as toimenpide}
  <NewToimenpideDialog
    id={valvonta.id}
    {toimenpide}
    {templatesByType}
    henkilot={Toimenpiteet.isOrder(toimenpide)
      ? henkilot
      : R.filter(Osapuolet.isOmistaja, henkilot)}
    yritykset={Toimenpiteet.isOrder(toimenpide)
      ? yritykset
      : R.filter(Osapuolet.isOmistaja, yritykset)}
    {roolit}
    {toimitustavat}
    {hallintoOikeudet}
    {karajaoikeudet}
    manuallyDeliverableToimenpide={isManuallyDeliverable(toimenpide)}
    commentingAllowed={isCommentingAllowed(toimenpide)}
    reload={load} />
{/each}

{#each Maybe.toArray(newNote) as note}
  <NoteDialog id={valvonta.id} {note} reload={load} />
{/each}

<div class="lg:w-1/2 w-full mb-5">
  <Select
    label={i18n('valvonta.valvoja')}
    model={valvonta}
    lens={R.lensProp('valvoja-id')}
    on:change={R.compose(
      saveKasittelija,
      R.map(parseInt),
      Maybe.fromNull,
      R.path(['target', 'value'])
    )}
    format={Kayttajat.format(i18n('valvonta.self'), valvojat, whoami)}
    items={R.pluck('id', Valvojat.filterActive(valvojat))} />
</div>

<div class="flex space-x-4 mb-5">
  <TextButton
    icon="add_comment"
    text={i18n('valvonta.add-muistiinpano')}
    on:click={openNewNote} />
</div>

{#if !isAuditCase(toimenpiteet) && !isClosed(toimenpiteet)}
  <div class="mb-5">
    <Button
      text={i18n('valvonta.aloita-valvonta')}
      prefix="start"
      on:click={_ => openNewToimenpide(Toimenpiteet.type.case)} />
  </div>
{:else if isClosed(toimenpiteet)}
  <div class="mb-5">
    <Button
      text={i18n('valvonta.jatka-valvontaa')}
      prefix="continue"
      on:click={_ => openNewToimenpide(Toimenpiteet.type.reopen)} />
  </div>
{:else}
  <H2 text={i18n('valvonta.new-toimenpide')} />

  {#if Toimenpiteet.isReopen(R.last(toimenpiteet))}
    <div class="warning-label mb-5">
      <span class="font-icon text-warning">warning</span>
      {i18n('valvonta.reopen-warning')}
    </div>
  {/if}

  <div class="lg:w-1/2 w-full mb-5">
    <PrimaryToimenpideContextProvider
      primaryOption={Locales.label($locale)(
        Toimenpiteet.primaryTransitionForToimenpidetype(
          toimenpiteet,
          toimenpidetyypit
        )
      )}>
      <Select
        label={i18n('valvonta.select-toimenpide')}
        name="toimenpide-type-selection"
        model={toimenpideTyyppi}
        lens={R.lens(R.identity, R.identity)}
        inputValueParse={R.prop('id')}
        format={Locales.label($locale)}
        itemComponent={ToimenpideSelectItem}
        on:change={event => openNewToimenpide(parseInt(event.target.value))}
        items={R.compose(
          Toimenpiteet.filterAvailableToimenpidetypes(toimenpiteet),
          R.filter(R.allPass([Toimenpiteet.isAuditCaseToimenpideType, isValid]))
        )(toimenpidetyypit)} />
    </PrimaryToimenpideContextProvider>
  </div>
{/if}
