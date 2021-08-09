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

  let overlay = true;
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
    Future.parallelObject(4, {
      henkilo: api.getHenkilo(params.id, params['kohde-id']),
      roolit: api.roolit,
      toimitustavat: api.toimitustavat,
      countries: geoApi.countries
    })
  );

  const submitHenkilo = (henkilo, event) => {
    if (henkilo.etunimi?.length >= 1 && henkilo.sukunimi?.length >= 1) {
      overlay = true;
      updateHenkilo(henkilo);
    } else {
      flashMessageStore.add(
        'henkilo',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const updateHenkilo = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.update-error`,
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
          i18n(`${i18nRoot}.messages.update-success`)
        );
        dirty = false;
        push(
          '/valvonta/kaytto/' + params['kohde-id'] + '/henkilo/' + params.id
        );
      }
    ),
    api.putHenkilo(fetch, params.id, params['kohde-id'])
  );

  const deleteHenkilo = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.delete-error`,
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
          i18n(`${i18nRoot}.messages.delete-success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + params['kohde-id'] + '/kohde');
      }
    ),
    api.deleteHenkilo(fetch, params.id, params['kohde-id'])
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { henkilo, roolit, toimitustavat, countries }}
      <H1 text={`${henkilo.etunimi} ${henkilo.sukunimi}`} />
      <HenkiloForm
        {henkilo}
        {roolit}
        {toimitustavat}
        {countries}
        {i18n}
        {i18nRoot}
        {submitHenkilo}
        {deleteHenkilo}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
