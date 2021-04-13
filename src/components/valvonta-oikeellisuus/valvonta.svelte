<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as ETViews from '@Component/Energiatodistus/views';

  import * as EnergiatodistusApi from '@Component/Energiatodistus/energiatodistus-api';
  import * as KayttajaApi from '@Component/Kayttaja/kayttaja-api';
  import * as ValvontaApi from '@Component/valvonta-oikeellisuus/valvonta-api';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Address from '@Component/Energiatodistus/address.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';

  import Manager from './manager.svelte';
  import Toimenpide from './toimenpide.svelte';

  const i18nRoot = 'valvonta.oikeellisuus.existing';

  export let params;
  export let resources = Maybe.None();
  let overlay = true;

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
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
      },
      response => {
        resources = Maybe.Some(response);
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
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <H1 text="Oikeellisuuden valvonta"/>

    {#each Maybe.toArray(resources) as { energiatodistus, luokittelut, toimenpiteet, toimenpidetyypit, valvojat, valvonta, whoami }}

      <div class="mb-2">
        Energiatodistus {energiatodistus.versio}/{energiatodistus.id} -
        {Maybe.orSome('', energiatodistus.perustiedot.nimi)} -
        {kayttotarkoitusTitle(R.objOf(energiatodistus.versio, luokittelut), energiatodistus)}
      </div>
      <div class="mb-5">
        <Address postinumerot={luokittelut.postinumerot} {energiatodistus} />
      </div>

      {#if Kayttajat.isPaakayttaja(whoami)}
        <Manager
            {energiatodistus}
            {valvojat} {valvonta}
            {toimenpiteet}
            {toimenpidetyypit}
            {saveValvonta}
            reload={_ => load(params)}/>
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