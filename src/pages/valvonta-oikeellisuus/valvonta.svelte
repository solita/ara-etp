<script>
  import * as R from 'ramda';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Kayttajat from '@Utility/kayttajat';

  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as ETUtils from '@Pages/energiatodistus/energiatodistus-utils';
  import * as ETViews from '@Pages/energiatodistus/views';
  import * as ViestiLinks from '@Pages/viesti/links';
  import * as Viestit from '@Pages/viesti/viesti-util';

  import * as EnergiatodistusApi from '@Pages/energiatodistus/energiatodistus-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as ValvontaApi from '@Pages/valvonta-oikeellisuus/valvonta-api';
  import * as ViestiApi from '@Pages/viesti/viesti-api';
  import RakennuksenNimi from '@Pages/energiatodistus/RakennuksenNimi';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Address from '@Pages/energiatodistus/address.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import HR from '@Component/HR/HR';

  import Manager from './manager.svelte';
  import LaatijaResponse from './laatija-response.svelte';
  import Toimenpide from './toimenpide.svelte';
  import Note from './note.svelte';
  import Link from '../../components/Link/Link.svelte';
  import * as ETValvonta from '@Pages/valvonta-oikeellisuus/energiatodistus-valvonta';

  const i18n = $_;
  const i18nRoot = 'valvonta.oikeellisuus.valvonta';

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
          ? i18n(`${i18nRoot}.messages.not-found`)
          : i18n(
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
      R.chain(
        whoami =>
          Future.parallelObject(5, {
            luokittelut: EnergiatodistusApi.luokittelutForVersion(
              params.versio
            ),
            ...ETValvonta.fetchEnergiatodistusWithKorvaavat(
              whoami,
              params.versio,
              params.id
            ),
            toimenpiteet: ValvontaApi.toimenpiteet(params.id),
            notes: Kayttajat.isPaakayttaja(whoami)
              ? ValvontaApi.notes(params.id)
              : Future.resolve([]),
            toimenpidetyypit: ValvontaApi.toimenpidetyypit,
            templatesByType: ValvontaApi.templatesByType,
            valvojat: ValvontaApi.valvojat,
            ketjut: ViestiApi.getEnergiatodistusKetjut(params.id),
            valvonta: ValvontaApi.valvonta(params.id),
            whoami: Future.resolve(whoami)
          }),
        KayttajaApi.whoami
      )
    );
  };

  const kayttotarkoitusTitle = ETViews.kayttotarkoitusTitle($locale);

  const fork = (key, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.${key}-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('valvonta-oikeellisuus', 'error', msg);
        overlay = false;
      },
      response => {
        flashMessageStore.add(
          'valvonta-oikeellisuus',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        overlay = false;
        successCallback(response);
      },
      future
    );
  };

  const diaarinumero = R.compose(
    R.chain(R.prop('diaarinumero')),
    Maybe.fromNull,
    R.last
  );

  const tapahtumat = R.compose(
    R.reverse,
    R.sortBy(R.prop('create-time')),
    R.unnest
  );

  const isToimenpide = R.has('type-id');

  const keyed = R.curry((prefix, tapahtuma) =>
    R.assoc('key', `${prefix}_${tapahtuma.id}`, tapahtuma)
  );

  $: saveValvonta = R.compose(
    fork('update', _ => load(params)),
    ValvontaApi.putValvonta(params.id)
  );

  const findketju = (toimenpide, ketjut) =>
    Maybe.find(R.propEq('vo-toimenpide-id', Maybe.Some(toimenpide.id)), ketjut);

  const ketjuIcon = ketju =>
    Viestit.hasUnreadViesti(ketju.viestit)
      ? Maybe.Some('mark_email_unread')
      : Maybe.Some('mail');
</script>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    {#each Maybe.toArray(resources) as { energiatodistus, korvaavatEnergiatodistukset, luokittelut, toimenpiteet, notes, ketjut, toimenpidetyypit, templatesByType, valvojat, valvonta, whoami }}
      <H1
        text={i18n(i18nRoot + '.title') +
          Maybe.fold('', R.concat(' - '), diaarinumero(toimenpiteet))} />
      <div class="flex flex-col mb-8">
        <div>{Maybe.orSome('', energiatodistus['laatija-fullname'])}</div>
        <div class="flex space-x-1">
          <div><RakennuksenNimi {energiatodistus} />.</div>
          <Address postinumerot={luokittelut.postinumerot} {energiatodistus} />
        </div>
        <div>
          {kayttotarkoitusTitle(
            R.objOf(energiatodistus.versio, luokittelut),
            energiatodistus
          )}
        </div>
        <div>
          {#if korvaavatEnergiatodistukset.length > 0}
            <div>
              <span>
                {i18n('energiatodistus.title')}
              </span>
              <Link
                bold={true}
                href={`/#/energiatodistus/${energiatodistus.versio}/${energiatodistus.id}`}
                text={energiatodistus.id} />
              <span>
                {Maybe.fold(
                  '',
                  Formats.formatTimeInstantMinutes,
                  energiatodistus.allekirjoitusaika
                )}
              </span>
            </div>
          {/if}
          {#each korvaavatEnergiatodistukset as korvaavaEt}
            <div>
              <span>
                {ETUtils.isDraft(korvaavaEt)
                  ? i18n('energiatodistus.korvaavuus.header.korvaava-draft')
                  : i18n('energiatodistus.korvaavuus.header.korvaava')}
              </span>
              <Link
                bold={true}
                href={`/#/energiatodistus/${korvaavaEt.versio}/${korvaavaEt.id}`}
                text={korvaavaEt.id} />
              <span>
                {Maybe.fold(
                  '',
                  Formats.formatTimeInstantMinutes,
                  korvaavaEt.allekirjoitusaika
                )}
              </span>
            </div>
          {/each}
        </div>
      </div>

      {#if Kayttajat.isPaakayttaja(whoami)}
        <Manager
          {energiatodistus}
          {valvojat}
          {valvonta}
          {toimenpiteet}
          {toimenpidetyypit}
          {templatesByType}
          {saveValvonta}
          {whoami}
          reload={_ => load(params)} />
      {/if}
      {#if Kayttajat.isLaatija(whoami)}
        <LaatijaResponse {energiatodistus} {toimenpiteet} {fork} />
      {/if}

      <HR compact={true} />

      <H2 text={i18n(i18nRoot + '.toimenpiteet')} />
      {#each tapahtumat( [R.map(keyed('toimenpide'), toimenpiteet), R.map(keyed('note'), notes)] ) as tapahtuma (tapahtuma.key)}
        <div class="mb-8">
          {#if isToimenpide(tapahtuma)}
            <Toimenpide
              {energiatodistus}
              {toimenpidetyypit}
              toimenpide={tapahtuma}
              {whoami}
              {i18n}
              reload={_ => load(params)}
              {valvojat} />
            {#each findketju(tapahtuma, ketjut).toArray() as ketju}
              <Link
                href={ViestiLinks.ketju(ketju)}
                icon={ketjuIcon(ketju)}
                text={ketju.subject} />
            {/each}
          {:else}
            <Note note={tapahtuma} {valvojat} {i18n} />
          {/if}
        </div>
      {/each}
      {#if R.isEmpty(tapahtumat([toimenpiteet, notes]))}
        <p>{i18n('valvonta.no-events')}</p>
      {/if}
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
