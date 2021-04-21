<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';
  import * as Router from '@Component/Router/router';

  import * as Toimenpiteet from './toimenpiteet';

  import NewToimenpideDialog from './new-toimenpide-dialog.svelte';

  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Select from '@Component/Select/Select.svelte';
  import H2 from '@Component/H/H2.svelte';

  export let energiatodistus;
  export let valvojat;
  export let valvonta;
  export let toimenpiteet;
  export let toimenpidetyypit;

  export let saveValvonta;
  export let reload;

  let newToimenpide = Maybe.None();
  let toimenpideTyyppi = Maybe.None();

  const openNewToimenpide = type => {
    if (Toimenpiteet.isDialogType(type)) {
      // toimenpiteet, jotka käsitellään dialogi-näytöllä
      newToimenpide = Maybe.Some({
        'type-id': type,
        'deadline-date': Either.Right(Toimenpiteet.defaultDeadline(type)),
        document: Maybe.None()
      });
    } else {
      // toimenpiteet, jotka käsitellään omalla näytöllä
      Router.push(
        `#/valvonta/oikeellisuus/${energiatodistus.versio}/${energiatodistus.id}/new/${type}`
      );
    }
  };

  const isAuditCase = toimenpiteet =>
    !R.isEmpty(toimenpiteet) && Toimenpiteet.isAuditCase(R.last(toimenpiteet));

  const saveKasittelija = id => saveValvonta({ 'valvoja-id': id });

  const fullName = valvojat =>
    R.compose(
      R.join(' '),
      R.juxt([R.prop('etunimi'), R.prop('sukunimi')]),
      R.find(R.__, valvojat),
      R.propEq('id')
    );

  const load = _ => {
    newToimenpide = Maybe.None();
    toimenpideTyyppi = Maybe.None();
    reload();
  };
</script>

{#each Maybe.toArray(newToimenpide) as toimenpide}
  <NewToimenpideDialog id={energiatodistus.id} {toimenpide} reload={load} />
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
  <TextButton icon="add_comment" text="Lisää muistiinpano" on:click={load} />
  <TextButton
    disabled={isAuditCase(toimenpiteet)}
    icon="verified"
    text="Merkitse katsotuksi"
    on:click={_ => openNewToimenpide(Toimenpiteet.type.verified)} />
  <TextButton
    disabled={isAuditCase(toimenpiteet)}
    icon="bug_report"
    text="Ilmoita poikkeamasta"
    on:click={_ => openNewToimenpide(Toimenpiteet.type.anomaly)} />
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
