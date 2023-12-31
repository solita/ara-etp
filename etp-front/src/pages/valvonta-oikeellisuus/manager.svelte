<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Locales from '@Language/locale-utils';
  import * as Valvojat from '@Pages/valvonta/valvojat';

  import { _, locale } from '@Language/i18n';
  import * as Router from '@Component/Router/router';

  import * as Kayttajat from '@Utility/kayttajat';
  import * as Toimenpiteet from './toimenpiteet';
  import * as ET from '@Pages/energiatodistus/energiatodistus-utils';
  import * as Links from './links';

  import NewToimenpideDialog from './new-toimenpide-dialog.svelte';
  import NoteDialog from './note-dialog.svelte';

  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Link from '@Component/Link/Link.svelte';
  import H2 from '@Component/H/H2.svelte';

  export let energiatodistus;
  export let valvojat;
  export let valvonta;
  export let toimenpiteet;
  export let toimenpidetyypit;
  export let templatesByType;
  export let whoami;

  export let saveValvonta;
  export let reload;
  const i18n = $_;

  let newToimenpide = Maybe.None();
  let newNote = Maybe.None();

  let toimenpideTyyppi = Maybe.None();

  const openNewToimenpide = type => {
    if (Toimenpiteet.isDialogType(type)) {
      // toimenpiteet, jotka käsitellään dialogi-näytöllä
      newToimenpide = Maybe.Some(Toimenpiteet.emptyToimenpide(type));
    } else {
      // toimenpiteet, jotka käsitellään omalla näytöllä
      Router.push(Links.newToimenpide(type, energiatodistus));
    }
  };

  const openNewNote = _ => {
    newNote = Maybe.Some({ description: '' });
  };

  const isAuditCase = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isAuditCase(R.last(toimenpiteet));

  const isDraft = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isDraft(R.last(toimenpiteet));

  const saveKasittelija = id => saveValvonta({ 'valvoja-id': id });

  const load = _ => {
    newToimenpide = Maybe.None();
    newNote = Maybe.None();
    toimenpideTyyppi = Maybe.None();
    reload();
  };
</script>

{#each Maybe.toArray(newToimenpide) as toimenpide}
  <NewToimenpideDialog
    id={energiatodistus.id}
    {toimenpide}
    {templatesByType}
    reload={load} />
{/each}

{#each Maybe.toArray(newNote) as note}
  <NoteDialog id={energiatodistus.id} {note} reload={load} />
{/each}

<div class="lg:w-1/4 w-full mb-8">
  <Select
    label={i18n('valvonta.valvoja')}
    model={valvonta}
    lens={R.lensProp('valvoja-id')}
    on:change={event => saveKasittelija(parseInt(event.target.value))}
    format={Kayttajat.format(i18n('valvonta.self'), valvojat, whoami)}
    items={R.pluck('id', Valvojat.filterActive(valvojat))} />
</div>

<div class="flex space-x-4 mb-8">
  <TextButton
    icon="add_comment"
    text={i18n('valvonta.add-muistiinpano')}
    on:click={openNewNote} />
  <TextButton
    disabled={isAuditCase(toimenpiteet) || ET.isDraft(energiatodistus)}
    icon="visibility"
    text={i18n('valvonta.oikeellisuus.valvonta.mark-as-verified')}
    on:click={_ => openNewToimenpide(Toimenpiteet.type.verified)} />
  <TextButton
    disabled={isAuditCase(toimenpiteet) || ET.isDraft(energiatodistus)}
    icon="bug_report"
    text={i18n('valvonta.oikeellisuus.valvonta.mark-anomaly')}
    on:click={_ => openNewToimenpide(Toimenpiteet.type.anomaly)} />
</div>

{#if !isAuditCase(toimenpiteet) && !ET.isDraft(energiatodistus)}
  <div class="mb-5">
    <Button
      text={i18n('valvonta.aloita-valvonta')}
      on:click={_ => openNewToimenpide(Toimenpiteet.type.case)} />
  </div>
{:else if !isDraft(toimenpiteet) && !ET.isDraft(energiatodistus)}
  <H2 text={i18n('valvonta.new-toimenpide')} />
  <div class="lg:w-1/2 w-full mb-5">
    <Select
      label={i18n('valvonta.select-toimenpide')}
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
{:else if isAuditCase && isDraft(toimenpiteet)}
  <div class="mb-5">
    Toimenpide <Link
      href={Links.toimenpide(R.last(toimenpiteet), energiatodistus)}
      text={R.compose($_, Toimenpiteet.i18nKey)(
        R.last(toimenpiteet),
        'title'
      )} /> on luonnos tilassa. Et voi tehdä uutta toimenpidettä ennen kuin edellinen
    toimenpide on suoritettu loppuun.
  </div>
{/if}
