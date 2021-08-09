<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import { push } from '@Component/Router/router';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import HenkiloForm from './henkilo-form.svelte';
  import { _ } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  export let params;

  import * as api from './valvonta-api';
  import * as geoApi from '@Component/Geo/geo-api';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.osapuolet';
  const emptyHenkilo = {
    etunimi: '',
    sukunimi: '',
    henkilotunnus: Maybe.None(),
    'rooli-id': Maybe.None(),
    'rooli-description': Maybe.None(),
    email: Maybe.None(),
    puhelin: Maybe.None(),
    'vastaanottajan-tarkenne': Maybe.None(),
    jakeluosoite: Maybe.None(),
    postinumero: Maybe.None(),
    postitoimipaikka: Maybe.None(),
    maa: Maybe.None(),
    'toimitustapa-id': Maybe.None(),
    'toimitustapa-description': Maybe.None()
  };

  let overlay = true;
  let henkilo = emptyHenkilo;
  let dirty = false;

  let resources = Maybe.None();

  Future.fork(
    response => {
      const msg = $_(
        `${i18nRoot}.messages.load-error`,
        Response.localizationKey(response)
      );

      flashMessageStore.add('henkilo', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(3, {
      roolit: api.roolit,
      toimitustavat: api.toimitustavat,
      countries: geoApi.countries
    })
  );

  const submitNewHenkilo = (henkilo, event) => {
    if (henkilo.etunimi?.length >= 1 && henkilo.sukunimi?.length >= 1) {
      overlay = true;
      addHenkilo(henkilo);
    } else {
      flashMessageStore.add(
        'henkilo',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const addHenkilo = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('henkilo', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'henkilo',
          'success',
          i18n(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + params['kohde-id'] + '/henkilo/' + _.id);
      }
    ),
    api.postHenkilo(fetch, params['kohde-id'])
  );
</script>

<H1 text={i18n(i18nRoot + '.uusi-henkilo')} />

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { roolit, toimitustavat, countries }}
      <HenkiloForm
        {henkilo}
        {roolit}
        {toimitustavat}
        {countries}
        {i18n}
        {i18nRoot}
        submitHenkilo={submitNewHenkilo}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
