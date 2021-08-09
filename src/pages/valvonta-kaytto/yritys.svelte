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
  import YritysForm from './yritys-form.svelte';
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

      flashMessageStore.add('yritys', 'error', msg);
      overlay = false;
    },
    response => {
      resources = Maybe.Some(response);
      overlay = false;
    },
    Future.parallelObject(4, {
      yritys: api.getYritys(params.id, params['kohde-id']),
      roolit: api.roolit,
      toimitustavat: api.toimitustavat,
      countries: geoApi.countries
    })
  );

  const submitYritys = (yritys, event) => {
    if (yritys.nimi?.length >= 1) {
      overlay = true;
      updateYritys(yritys);
    } else {
      flashMessageStore.add(
        'yritys',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const updateYritys = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.update-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('yritys', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'yritys',
          'success',
          i18n(`${i18nRoot}.messages.update-success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + params['kohde-id'] + '/yritys/' + params.id);
      }
    ),
    api.putYritys(fetch, params.id, params['kohde-id'])
  );

  const deleteYritys = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.delete-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('yritys', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'yritys',
          'success',
          i18n(`${i18nRoot}.messages.delete-success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + params['kohde-id'] + '/kohde');
      }
    ),
    api.deleteYritys(fetch, params.id, params['kohde-id'])
  );
</script>

<Overlay {overlay}>
  <div slot="content">
    <DirtyConfirmation {dirty} />
    {#each Maybe.toArray(resources) as { yritys, roolit, toimitustavat, countries }}
      <H1 text={yritys.nimi} />
      <YritysForm
        {yritys}
        {roolit}
        {toimitustavat}
        {countries}
        {i18n}
        {i18nRoot}
        {submitYritys}
        {deleteYritys}
        bind:dirty />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
