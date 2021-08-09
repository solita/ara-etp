<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import * as Locales from '@Language/locale-utils';
  import { push } from '@Component/Router/router';

  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Confirm from '@Component/Confirm/Confirm';
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Input from '@Component/Input/Input.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Datepicker from '@Component/Input/Datepicker';
  import Liitteet from '@Component/liitteet/liitteet.svelte';
  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as api from './valvonta-api';
  import * as KayttajaApi from '@Pages/Kayttaja/kayttaja-api';

  export let params;

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';

  let overlay = true;
  let dirty = false;
  let form;
  let kohde;

  let resources = Maybe.None();
  const enableOverlay = _ => {
    overlay = true;
  };

  const load = id => {
    Future.fork(
      response => {
        const msg = $_(
          `${i18nRoot}.messages.load-error`,
          Response.localizationKey(response)
        );

        flashMessageStore.add('kohde', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        kohde = response.kohde;
        overlay = false;
      },
      Future.parallelObject(4, {
        kohde: api.getKaytto(id),
        liitteet: api.getKayttoLiitteet(id),
        whoami: KayttajaApi.whoami,
        ilmoituspaikat: api.ilmoituspaikat
      })
    );
  };

  const submitKohde = event => {
    if (kohde.katuosoite?.length >= 3) {
      overlay = true;
      kohde = R.dissoc('id', kohde);
      updateKohde(kohde);
    } else {
      flashMessageStore.add(
        'kohde',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };
  const revertForm = event => {
    kohde = R.prop('kohde', R.head(Maybe.toArray(resources)));
    Validation.blurForm(event.target);
  };

  const updateKohde = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.save-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('kohde', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'kohde',
          'success',
          i18n(`${i18nRoot}.messages.save-success`)
        );
        dirty = false;
        load(params.id);
      }
    ),
    R.tap(enableOverlay),
    api.putKaytto(fetch, params.id)
  );

  const deleteKohde = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.delete-error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('kohde', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'kohde',
          'success',
          i18n(`${i18nRoot}.messages.delete-success`)
        );
        dirty = false;
        push('/valvonta/kaytto/all');
      }
    ),
    R.tap(enableOverlay),
    api.deleteKaytto(fetch, params.id)
  );

  const errorMessage = (key, response) =>
    i18n(
      Response.notFound(response)
        ? `${i18nRoot}.messages.not-found`
        : Maybe.orSome(
            `${i18nRoot}.messages.${key}-error`,
            Response.localizationKey(response)
          )
    );

  const fork = (key, successCallback) => future => {
    overlay = true;
    Future.fork(
      response => {
        const msg = errorMessage(key, response);
        flashMessageStore.add('valvonta-kaytto', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'valvonta-kaytto',
          'success',
          i18n(`${i18nRoot}.messages.${key}-success`)
        );
        successCallback();
      },
      future
    );
  };

  const liiteOperation = (key, liiteFuture) => liite =>
    fork(key, _ => load(params.id))(Future.parallel(1, [liiteFuture(liite)]));

  const liiteApi = {
    getUrl: api.url.liitteet(params.id),

    addFiles: liiteOperation(
      'add-files',
      api.postKayttoLiitteetFiles(params.id)
    ),

    addLink: liiteOperation('add-link', api.postKayttoLiitteetLink(params.id)),

    deleteLiite: liiteOperation(
      'delete-liite',
      api.deleteKayttoLiite(params.id)
    )
  };

  $: load(params.id);
</script>

<!-- <H1 text={i18n(i18nRoot + '.title')} /> -->

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { whoami, liitteet, ilmoituspaikat }}
      <form
        class="content"
        bind:this={form}
        on:submit|preventDefault={submitKohde}
        on:input={_ => {
          dirty = true;
        }}
        on:change={_ => {
          dirty = true;
        }}>
        <DirtyConfirmation {dirty} />
        <div class="flex flex-col w-full py-8">
          <H2 text={i18n(`${i18nRoot}.rakennuksen-tiedot`)} />
          <div class="py-4 w-full md:w-1/3">
            <Input
              id={'kohde.rakennustunnus'}
              name={'kohde.rakennustunnus'}
              label={i18n(`${i18nRoot}.rakennustunnus`)}
              bind:model={kohde}
              lens={R.lensProp('rakennustunnus')}
              parse={Parsers.optionalString}
              format={Maybe.orSome('')}
              {i18n} />
          </div>
          <div class="py-4 w-full md:w-1/2">
            <Input
              id={'kohde.katuosoite'}
              name={'kohde.katuosoite'}
              label={i18n(`${i18nRoot}.katuosoite`)}
              required={true}
              bind:model={kohde}
              lens={R.lensProp('katuosoite')}
              parse={R.trim}
              {i18n} />
          </div>
          <div class="py-4 w-full md:w-1/3">
            <Input
              id={'kohde.postinumero'}
              name={'kohde.postinumero'}
              label={i18n(`${i18nRoot}.postinumero`)}
              bind:model={kohde}
              lens={R.lensProp('postinumero')}
              parse={Parsers.optionalString}
              format={Maybe.orSome('')}
              {i18n} />
          </div>
        </div>
        <div class="flex flex-col w-full py-8">
          <H2 text={i18n(`${i18nRoot}.ilmoituksen-tiedot`)} />

          <div class="py-4 w-full md:w-1/3">
            <Datepicker
              label={i18n(`${i18nRoot}.havaintopaiva`)}
              bind:model={kohde}
              lens={R.lensProp('havaintopaiva')}
              format={Maybe.fold('', Formats.formatDateInstant)}
              parse={Parsers.optionalParser(Parsers.parseDate)}
              transform={EM.fromNull}
              {i18n} />
          </div>
          <div class="py-4 w-full md:w-1/3">
            <Select
              id={'kohde.ilmoituspaikka-id'}
              label={i18n(`${i18nRoot}.ilmoituspaikka-id`)}
              required={false}
              disabled={false}
              allowNone={true}
              bind:model={kohde}
              parse={Maybe.fromNull}
              lens={R.lensProp('ilmoituspaikka-id')}
              format={Locales.labelForId($locale, ilmoituspaikat)}
              items={R.pluck('id', ilmoituspaikat)} />
          </div>
          {#each Maybe.toArray(kohde['ilmoituspaikka-id']) as ilmoituspaikkaId}
            {#if ilmoituspaikkaId === 2}
              <div class="py-4 w-full md:w-1/3">
                <Input
                  id={'kohde.ilmoituspaikka-description'}
                  name={'kohde.ilmoituspaikka-description'}
                  label={i18n(`${i18nRoot}.ilmoituspaikka-description`)}
                  bind:model={kohde}
                  lens={R.lensProp('ilmoituspaikka-description')}
                  parse={R.trim}
                  {i18n} />
              </div>
            {/if}
          {/each}
          <div class="py-4 w-full md:w-1/3">
            <Input
              id={'kohde.ilmoitustunnus'}
              name={'kohde.ilmoitustunnus'}
              label={i18n(`${i18nRoot}.ilmoitustunnus`)}
              bind:model={kohde}
              lens={R.lensProp('ilmoitustunnus')}
              parse={Parsers.optionalString}
              format={Maybe.orSome('')}
              {i18n} />
          </div>
        </div>
        <div class="flex space-x-4 py-8">
          <Button
            disabled={!dirty}
            type={'submit'}
            text={i18n(`${i18nRoot}.save`)} />
          <Button
            disabled={!dirty}
            on:click={revertForm}
            text={i18n(`${i18nRoot}.revert`)}
            style={'secondary'} />
          <Confirm
            let:confirm
            confirmButtonLabel={i18n('confirm.button.delete')}
            confirmMessage={i18n('confirm.you-want-to-delete')}>
            <Button
              on:click={() => {
                confirm(deleteKohde);
              }}
              text={i18n(`${i18nRoot}.delete`)}
              style={'error'} />
          </Confirm>
        </div>
        <div class="flex flex-col">
          <Liitteet
            {liiteApi}
            {liitteet}
            emptyMessageKey={i18n(`${i18nRoot}.no-attachments`)}
            flashModule="valvonta-kaytto" />
        </div>
      </form>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
