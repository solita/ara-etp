<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';

  import * as Toimenpiteet from './toimenpiteet';

  import NewToimenpideDialog from './new-toimenpide-dialog.svelte';
  import NoteDialog from './note-dialog.svelte';

  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Select from '@Component/Select/Select.svelte';
  import H2 from '@Component/H/H2.svelte';

  export let valvojat;
  export let valvonta;
  export let toimenpiteet;
  export let toimenpidetyypit;
  export let templatesByType;

  export let saveValvonta;
  export let reload;

  let newToimenpide = Maybe.None();
  let newNote = Maybe.None();

  let toimenpideTyyppi = Maybe.None();

  const openNewToimenpide = type => {
    newToimenpide = Maybe.Some(Toimenpiteet.emptyToimenpide(type));
  };

  const openNewNote = _ => {
    newNote = Maybe.Some({ description: '' });
  };

  const isAuditCase = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isAuditCase(R.last(toimenpiteet));

  const saveKasittelija = id =>
    saveValvonta(R.assoc('valvoja-id', id, valvonta));

  const fullName = valvojat =>
    R.compose(
      R.join(' '),
      R.juxt([R.prop('etunimi'), R.prop('sukunimi')]),
      R.find(R.__, valvojat),
      R.propEq('id')
    );

  const load = _ => {
    newToimenpide = Maybe.None();
    newNote = Maybe.None();
    toimenpideTyyppi = Maybe.None();
    reload();
  };
</script>

{#each Maybe.toArray(newToimenpide) as toimenpide}
  <NewToimenpideDialog
    id={valvonta.id}
    {toimenpide}
    {templatesByType}
    reload={load} />
{/each}

{#each Maybe.toArray(newNote) as note}
  <NoteDialog id={valvonta.id} {note} reload={load} />
{/each}

<div class="lg:w-1/2 w-full mb-5">
  <Select
    label="Valitse käsittelijä"
    model={valvonta}
    lens={R.lensProp('valvoja-id')}
    on:change={event => saveKasittelija(parseInt(event.target.value))}
    format={fullName(valvojat)}
    items={R.pluck('id', valvojat)} />
</div>

<div class="flex space-x-4 mb-5">
  <TextButton
    icon="add_comment"
    text="Lisää muistiinpano"
    on:click={openNewNote} />
</div>

{#if !isAuditCase(toimenpiteet)}
  <div class="mb-5">
    <Button
      text="Aloita valvonta"
      on:click={_ => openNewToimenpide(Toimenpiteet.type.case)} />
  </div>
{:else}
  <H2 text="Uusi toimenpide" />

  <div class="lg:w-1/2 w-full mb-5">
    <Select
      label="Valitse toimenpide"
      model={toimenpideTyyppi}
      lens={R.lens(R.identity, R.identity)}
      inputValueParse={R.prop('id')}
      format={Locales.label($locale)}
      on:change={event => openNewToimenpide(parseInt(event.target.value))}
      items={R.filter(
        Toimenpiteet.isAuditCaseToimenpideType,
        toimenpidetyypit
      )} />
  </div>
{/if}
