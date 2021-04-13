<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Locales from '@Language/locale-utils';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';
  import * as Router from '@Component/Router/router';

  import * as ETViews from '@Component/Energiatodistus/views';
  import * as Toimenpiteet from './toimenpiteet';

  import * as EnergiatodistusApi from '@Component/Energiatodistus/energiatodistus-api';
  import * as KayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as ValvontaApi from '@Component/valvonta-oikeellisuus/valvonta-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';

  import NewToimenpideDialog from './new-toimenpide-dialog.svelte';
  import Toimenpide from './toimenpide.svelte';

  import Button from '@Component/Button/Button.svelte';
  import TextButton from '@Component/Button/TextButton.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Address from '@Component/Energiatodistus/address.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.existing';

  export let params;
  export let resources = Maybe.None();

  let overlay = true;
  let newToimenpide = Maybe.None();

  $: load(params);

  const load = params => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        const msg = Response.notFound(response)
          ? $_(`${i18nRoot}.messages.not-found`)
          : $_(
            Maybe.orSome(
              `${i18nRoot}.messages.load-error`,
              Response.localizationKey(response)
            )
          );
        newToimenpide = Maybe.None();
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
      },
      response => {
        resources = Maybe.Some(response);
        newToimenpide = Maybe.None();
        overlay = false;
      },
      Future.parallelObject(5, {
        luokittelut: EnergiatodistusApi.luokittelutForVersion(params.version),
        energiatodistus: EnergiatodistusApi.getEnergiatodistusById(params.version, params.id),
        toimenpiteet: ValvontaApi.toimenpiteet(params.id),
        toimenpidetyypit: ValvontaApi.toimenpidetyypit,
        valvojat: ValvontaApi.valvojat,
        valvonta: ValvontaApi.valvonta(params.id),
        whoami: KayttajaApi.whoami
      })
    );
  };

  const kayttotarkoitusTitle = ETViews.kayttotarkoitusTitle($locale);

  const fullName = valvojat => R.compose(
    R.join(' '),
    R.juxt([R.prop('etunimi'), R.prop('sukunimi')]),
    R.find(R.__, valvojat),
    R.propEq('id'));

  const openNewToimenpide = type => {
    if (type == Toimenpiteet.type.audit.report) {
      // toimenpiteet, jotka käsitellään omalla näytöllä
      Router.push(`#/valvonta/oikeellisuus/${params.version}/${params.id}/new/${Toimenpiteet.type.audit.report}`);
    } else {
      // toimenpiteet, jotka käsitellään dialogi-näytöllä
      newToimenpide = Maybe.Some({
        'type-id': type,
        'deadline-date': Either.Right(Toimenpiteet.defaultDeadline(type)),
        'document': Maybe.None()
      });
    }
  };

  const isAuditCase = toimenpiteet => !R.isEmpty(toimenpiteet) && Toimenpiteet.isAuditCase(R.last(toimenpiteet));

  const saveValvonta = valvonta => {
    overlay = true;
    Future.fork(
      response => {
        const msg = $_(
          Maybe.orSome(
            `${i18nRoot}.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          $_(`${i18nRoot}.messages.update-success`)
        );
        load(params);
      },
      ValvontaApi.putValvonta(params.id, valvonta)
    )
  };

  const saveKasittelija = id => saveValvonta({ 'valvoja-id': id});
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text="Oikeellisuuden valvonta"/>
      {#each Maybe.toArray(newToimenpide) as toimenpide}
        <NewToimenpideDialog id={params.id} {toimenpide} reload={_ => load(params)}/>
      {/each}

    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, toimenpiteet, toimenpidetyypit, valvojat, valvonta, whoami }}

      <div class="mb-2">
        Energiatodistus {energiatodistus.versio}/{energiatodistus.id} -
        {Maybe.orSome('', energiatodistus.perustiedot.nimi)} -
        {kayttotarkoitusTitle(R.objOf(energiatodistus.versio, luokittelut), energiatodistus)}
      </div>
      <div class="mb-5">
        <Address postinumerot={luokittelut.postinumerot} {energiatodistus} />
      </div>

      <div class="lg:w-1/2 w-full mb-5">
        <Select label="Valitse käsittelijä"
                model={valvonta} lens={R.lensProp('valvoja-id')}
                on:change={event => saveKasittelija(parseInt(event.target.value))}
                format={fullName(valvojat)}
                items={R.pluck('id', valvojat)}/>
      </div>

      <div class="flex space-x-4 mb-5">
        <TextButton icon="add_comment" text="Lisää muistiinpano" on:click={_ => load(params)} />
        <TextButton disabled={isAuditCase(toimenpiteet)}
                    icon="verified"
                    text="Merkitse katsotuksi"
                    on:click={_ => openNewToimenpide(Toimenpiteet.type.verified)} />
        <TextButton disabled={isAuditCase(toimenpiteet)}
                    icon="bug_report"
                    text="Ilmoita poikkeamasta"
                    on:click={_ => openNewToimenpide(Toimenpiteet.type.anomaly)} />
      </div>

      {#if !isAuditCase(toimenpiteet)}
        <div class="mb-5">
          <Button text="Aloita valvonta" on:click={_ => openNewToimenpide(Toimenpiteet.type.case)} />
        </div>
      {:else}
        <H2 text="Uusi toimenpide"/>
        <div class="lg:w-1/2 w-full mb-5">
          <Select label="Valitse toimenpide"
                  model={Maybe.None()} lens={R.lens(R.identity, R.identity)}
                  inputValueParse={R.prop('id')}
                  format={Locales.label($locale)}
                  on:change={event => openNewToimenpide(parseInt(event.target.value))}
                  items={R.filter(Toimenpiteet.isAuditCaseToimenpideType, toimenpidetyypit)}/>
        </div>
      {/if}

      <H2 text="Toimenpiteet"/>
      {#each R.reverse(toimenpiteet) as toimenpide}
        <Toimenpide {toimenpidetyypit} {toimenpide}/>
      {/each}
      {#if R.isEmpty(toimenpiteet)}
        <p>Ei valvontatoimenpiteitä</p>
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>