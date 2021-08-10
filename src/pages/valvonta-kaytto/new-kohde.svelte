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
  import Overlay from '@Component/Overlay/Overlay.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import H1 from '@Component/H/H1.svelte';
  import H2 from '@Component/H/H2.svelte';
  import Input from '@Component/Input/Input.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Datepicker from '@Component/Input/Datepicker';
  import { _, locale } from '@Language/i18n';
  import { flashMessageStore } from '@/stores';

  import * as api from './valvonta-api';
  import * as KayttajaApi from '@Pages/kayttaja/kayttaja-api';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';
  const emptyKohde = {
    rakennustunnus: Maybe.None(),
    katuosoite: '',
    postinumero: Maybe.None(),
    'ilmoituspaikka-id': Maybe.None(),
    'ilmoituspaikka-description': Maybe.None(),
    ilmoitustunnus: Maybe.None(),
    havaintopaiva: Either.Right(Maybe.None()),
    'valvoja-id': Maybe.None()
  };

  let overlay = true;
  let dirty = false;
  let form;
  let kohde = emptyKohde;

  let resources = Maybe.None();

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
      overlay = false;
    },
    Future.parallelObject(2, {
      whoami: KayttajaApi.whoami,
      ilmoituspaikat: api.ilmoituspaikat
    })
  );

  const submitNewKohde = event => {
    if (kohde.katuosoite?.length >= 3) {
      overlay = true;
      addKohde(kohde);
    } else {
      flashMessageStore.add(
        'kohde',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };
  const resetForm = event => {
    kohde = emptyKohde;
    Validation.blurForm(event.target);
  };

  const addKohde = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
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
          i18n(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        push('/valvonta/kaytto/' + _.id + '/kohde');
      }
    ),
    api.postKaytto(fetch)
  );
</script>

<H1 text={i18n(i18nRoot + '.new-kohde')} />

<Overlay {overlay}>
  <div slot="content">
    {#each Maybe.toArray(resources) as { ilmoituspaikat }}
      <form
        class="content"
        bind:this={form}
        on:submit|preventDefault={submitNewKohde}
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
            on:click={resetForm}
            text={i18n(`${i18nRoot}.reset`)}
            type={'reset'}
            style={'secondary'} />
        </div>
        <div class="flex">
          <span>{i18n(`${i18nRoot}.after-save-notice`)}</span>
        </div>
      </form>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
