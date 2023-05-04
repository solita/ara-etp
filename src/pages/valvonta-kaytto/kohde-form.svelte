<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Validation from '@Utility/validation';
  import * as Locales from '@Language/locale-utils';
  import * as Ilmoituspaikka from './ilmoituspaikka';
  import * as Postinumero from '@Component/address/postinumero-fi';

  import { _, locale } from '@Language/i18n';
  import { kohde as kohdeSchema } from '@Pages/valvonta-kaytto/schema';

  import H2 from '@Component/H/H2.svelte';
  import Input from '@Component/Input/Input.svelte';
  import Select from '@Component/Select/Select.svelte';
  import Datepicker from '@Component/Input/Datepicker';
  import Button from '@Component/Button/Button.svelte';
  import Confirm from '@Component/Confirm/Confirm.svelte';
  import Link from '@Component/Link/Link.svelte';
  import Spinner from '@Component/Spinner/Spinner';

  import { flashMessageStore } from '@/stores';
  import Autocomplete from '../../components/Autocomplete/Autocomplete.svelte';
  import * as etApi from '@Pages/energiatodistus/energiatodistus-api';
  import * as ValvontaApi from './valvonta-api';

  const i18n = $_;
  const i18nRoot = 'valvonta.kaytto.kohde';

  export let kohde;
  export let ilmoituspaikat;
  export let postinumerot;

  export let save;
  export let revert;
  export let remove = Maybe.None();
  export let dirty;
  export let isNew = true;

  let form;
  let showRakennustunnusSpinner = false;
  let etForRakennustunnus = [];
  let existingValvonnatForRakennustunnus = Maybe.None();

  $: rakennustunnus = kohde.rakennustunnus;
  $: getEnergiatodistuksetByRakennustunnus(rakennustunnus);

  const getEnergiatodistuksetByRakennustunnus = R.forEach(rakennustunnus => {
    showRakennustunnusSpinner = true;
    Future.fork(
      ({ energiatodistukset }) => {
        showRakennustunnusSpinner = false;
        flashMessageStore.add(
          'valvonta-kaytto',
          'error',
          i18n(
            Response.errorKey(
              i18nRoot,
              'find-rakennustunnus',
              energiatodistukset
            )
          )
        );
      },
      ({ energiatodistukset, valvonnat }) => {
        showRakennustunnusSpinner = false;
        etForRakennustunnus = energiatodistukset;
        existingValvonnatForRakennustunnus = Maybe.Some(valvonnat);
      },
      Future.parallelObject(2, {
        energiatodistukset: etApi.getEnergiatodistukset(
          `?where=${encodeURI(
            `[[["ilike","energiatodistus.perustiedot.rakennustunnus","${rakennustunnus}"],["in","energiatodistus.tila-id", [0,1,2]]]]`
          )}&limit=11&order=asc&sort=energiatodistus.id&offset=0`
        ),
        valvonnat: ValvontaApi.getValvonnatByRakennusTunnus(rakennustunnus)
      })
    );
  });

  const submit = _ => {
    if (Validation.isValidForm(schema)(kohde)) {
      save(kohde);
    } else {
      flashMessageStore.add(
        'valvonta-kaytto',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(form);
    }
  };

  const setDirty = _ => {
    dirty = true;
  };

  $: postinumeroNames = R.map(Postinumero.fullLabel($locale), postinumerot);
  $: PostinumeroType = Postinumero.Type(postinumerot);
  $: schema = R.assoc('postinumero', PostinumeroType.validators, kohdeSchema);
</script>

<form
  class="content"
  bind:this={form}
  on:submit|preventDefault={submit}
  on:input={setDirty}
  on:change={setDirty}>
  <div class="flex flex-col w-full py-8">
    <H2 text={i18n(`${i18nRoot}.rakennuksen-tiedot`)} />

    <div class="py-4 w-full flex flex-col md:flex-row md:space-x-4">
      <div class="w-full md:w-1/3">
        <Input
          id={'kohde.rakennustunnus'}
          name={'kohde.rakennustunnus'}
          label={i18n(`${i18nRoot}.rakennustunnus`)}
          bind:model={kohde}
          lens={R.lensProp('rakennustunnus')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          validators={schema.rakennustunnus}
          {i18n} />
      </div>
      <div class="w-full md:w-2/3">
        {#if showRakennustunnusSpinner}
          <Spinner smaller={true} />
        {/if}
        {#if !R.isEmpty(etForRakennustunnus)}
          <div class="flex flex-col">
            <div class="flex items-center">
              <span class="font-icon mr-1 text-xl">info</span>
              <span>{i18n(`${i18nRoot}.rakennustunnus-existing-et`)}</span>
            </div>
            <div class="flex space-x-1 pl-6">
              {#each etForRakennustunnus as et}
                <Link
                  bold={true}
                  href={`/#/energiatodistus/${et.id}`}
                  text={et.id} />
              {/each}
              {#if etForRakennustunnus.length > 10}
                <span>...</span>
              {/if}
            </div>
          </div>
        {/if}
      </div>
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
        validators={schema.katuosoite}
        {i18n} />
    </div>
    <div class="py-4 w-full md:w-1/3">
      <Autocomplete items={postinumeroNames} size={10}>
        <Input
          id={'kohde.postinumero'}
          name={'kohde.postinumero'}
          label={i18n(`${i18nRoot}.postinumero`)}
          search={true}
          bind:model={kohde}
          lens={R.lensProp('postinumero')}
          parse={PostinumeroType.parse}
          format={Maybe.fold(
            '',
            Postinumero.formatPostinumero(postinumerot, $locale)
          )}
          validators={schema.postinumero}
          {i18n} />
      </Autocomplete>
    </div>

    {#if isNew && existingValvonnatForRakennustunnus.isSome()}
      <div>
        <h3>Aiemmat valvonnat</h3>
        {#each existingValvonnatForRakennustunnus.some() as valvonta, index}
          <Link
            href={`#/valvonta/kaytto/${valvonta.id}/valvonta`}
            target="_blank"
            text={valvonta.id} />
        {/each}
      </div>
    {/if}
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
    {#if Ilmoituspaikka.other(kohde)}
      <div class="py-4 w-full md:w-1/3">
        <Input
          id={'kohde.ilmoituspaikka-description'}
          name={'kohde.ilmoituspaikka-description'}
          label={i18n(`${i18nRoot}.ilmoituspaikka-description`)}
          bind:model={kohde}
          lens={R.lensProp('ilmoituspaikka-description')}
          parse={Parsers.optionalString}
          format={Maybe.orSome('')}
          validators={schema['ilmoituspaikka_description']}
          {i18n} />
      </div>
    {/if}
    <div class="py-4 w-full md:w-1/3">
      <Input
        id={'kohde.ilmoitustunnus'}
        name={'kohde.ilmoitustunnus'}
        label={i18n(`${i18nRoot}.ilmoitustunnus`)}
        bind:model={kohde}
        lens={R.lensProp('ilmoitustunnus')}
        parse={Parsers.optionalString}
        format={Maybe.orSome('')}
        validators={schema.ilmoitustunnus}
        {i18n} />
    </div>
  </div>
  <div class="flex space-x-4 py-8">
    <Button disabled={!dirty} type={'submit'} text={i18n(`${i18nRoot}.save`)} />
    <Button
      disabled={!dirty}
      on:click={revert}
      text={i18n(`${i18nRoot}.revert`)}
      style={'secondary'} />
    {#each Maybe.toArray(remove) as deleteKohde}
      <Confirm
        let:confirm
        confirmButtonLabel={i18n('confirm.button.delete')}
        confirmMessage={i18n('confirm.you-want-to-delete')}>
        <Button
          on:click={() => {
            confirm(_ => deleteKohde(kohde.id));
          }}
          text={i18n(`${i18nRoot}.delete`)}
          style={'error'} />
      </Confirm>
    {/each}
  </div>
</form>
