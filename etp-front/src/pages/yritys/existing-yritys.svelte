<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import YritysForm from '@Pages/yritys/yritys-form';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  import { idTranslateStore } from '@/stores';
  import { announcementsForModule } from '@Utility/announce';

  import * as api from '@Pages/yritys/yritys-api';
  import * as Yritykset from '@Pages/yritys/yritys-utils';
  import * as Locales from '@Language/locale-utils';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import H1 from '@Component/H/H1.svelte';

  const i18n = $_;
  const i18nRoot = 'yritys';
  const { announceError, announceSuccess } = announcementsForModule('yritys');

  export let params;

  let resources = Maybe.None();
  let dirty = false;

  let overlay = true;
  let disabled = true;

  const fork = (action, errorMessage, future) => {
    overlay = true;
    Future.fork(
      response => {
        announceError(errorMessage(response));
        overlay = false;
      },
      _ => {
        announceSuccess(i18n(`${i18nRoot}.messages.${action}-success`));
        overlay = false;
        load(params.id);
      },
      future
    );
  };

  const submit = updatedYritys =>
    fork(
      'save',
      response =>
        Locales.uniqueViolationMessage(
          i18n,
          response,
          'yritys.messages.save-error'
        ),
      api.putYritysById(fetch, params.id, updatedYritys)
    );

  const setDeleted = deleted =>
    fork(
      deleted ? 'delete' : 'undelete',
      response => Response.errorKey(i18nRoot, 'delete', response),
      api.putDeleted(params.id, deleted)
    );

  // Load yritys and all luokittelut used in yritys form
  const load = id => {
    overlay = true;
    Future.fork(
      response => {
        overlay = false;
        announceError(i18n(Response.errorKey404(i18nRoot, 'load', response)));
      },
      response => {
        resources = Maybe.Some(response);
        overlay = false;
        dirty = false;
        disabled = !Yritykset.hasModifyPermission(
          response.laatijat,
          response.whoami
        );
        idTranslateStore.updateYritys(response.yritys);
      },
      Future.parallelObject(4, {
        luokittelut: api.luokittelut,
        whoami: kayttajaApi.whoami,
        yritys: R.map(
          R.over(R.lensProp('verkkolaskuoperaattori'), Either.Right),
          api.getYritysById(id)
        ),
        laatijat: api.getLaatijatById(fetch, id)
      })
    );
  };

  $: load(params.id);
</script>

<Overlay {overlay}>
  <div slot="content" class="mt-3">
    {#each Maybe.toArray(resources) as { yritys, luokittelut, whoami }}
      <H1 text={yritys.nimi} />
      <YritysForm
        {submit}
        setDeleted={Maybe.Some(setDeleted)}
        cancel={_ => load(params.id)}
        {disabled}
        bind:dirty
        {luokittelut}
        {whoami}
        existing={false}
        {yritys} />
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
