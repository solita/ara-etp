<script>
  import * as R from 'ramda';
  import * as Schema from '@Pages/kayttaja/schema';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Maybe from '@Utility/maybe-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Validation from '@Utility/validation';
  import * as Locales from '@Language/locale-utils';
  import * as Formats from '@Utility/formats';

  import { locale, _ } from '@Language/i18n';

  import ApiKey from './api-key.svelte';
  import H2 from '@Component/H/H2';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox.svelte';
  import Select from '@Component/Select/Select2';
  import Datepicker from '@Component/Input/Datepicker';
  import * as Parsers from '@Utility/parsers';
  import { announcementsForModule } from '@Utility/announce';

  /*
   * Note: kayttaja.rooli :: Maybe[Id]
   * Otherwise kayttaja is the same as defined in
   * get api/private/kayttajat/:id and KayttajaApi.deserialize
   */
  export let kayttaja;
  export let aineistot;
  export let kayttajaAineistot;
  export let dirty;
  export let submit;
  export let cancel;
  export let whoami;

  const i18n = $_;
  const i18nRoot = 'kayttaja';
  const { announceError, clearAnnouncements } =
    announcementsForModule('kayttaja');

  const schema = Schema.Aineistoasiakas;
  const aineistoPermitSchema = Schema.aineistolupa;

  $: isValidForm = Validation.isValidForm(schema);
  $: isValidAineisto = Validation.isValidForm(aineistoPermitSchema);

  $: isPaakayttaja = Kayttajat.isPaakayttaja(whoami);
  $: isOwnSettings = R.eqProps('id', kayttaja, whoami);

  $: disabled = !isPaakayttaja && !isOwnSettings;
  $: disabledAdmin = !isPaakayttaja;

  $: if (Maybe.exists(Kayttajat.isLaatijaRole, kayttaja.rooli)) {
    throw 'This form should not be used for laatija.';
  }

  let form;
  const saveKayttaja = _ => {
    if (isValidForm(kayttaja) && isValidAineisto(kayttajaAineistot)) {
      clearAnnouncements();
      submit(R.evolve({ rooli: Maybe.get }, kayttaja), kayttajaAineistot);
    } else {
      announceError(i18n('kayttaja.messages.validation-error'));
      Validation.blurForm(form);
    }
  };

  const addNewAineisto = _ => {
    kayttajaAineistot = R.append(
      { 'aineisto-id': 1, 'valid-until': Maybe.None(), 'ip-address': '' },
      kayttajaAineistot
    );
  };

  const deleteAineisto = aineistoIndex => _ => {
    kayttajaAineistot = R.remove(aineistoIndex, 1, kayttajaAineistot);
    setDirty();
  };

  const setDirty = _ => {
    dirty = true;
  };

  $: maximumNumberOfIPsGiven = kayttajaAineistot.length >= 10;
</script>

<form
  bind:this={form}
  on:submit|preventDefault={saveKayttaja}
  on:input={setDirty}
  on:change={setDirty}>
  <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Checkbox
        bind:model={kayttaja}
        lens={R.lensProp('passivoitu')}
        label={i18n(i18nRoot + '.passivoitu')}
        disabled={disabledAdmin || isOwnSettings} />
    </div>
  </div>

  <H2 text={i18n('kayttaja.perustiedot-header')} />

  <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Input
        id={'organisaatio'}
        name={'organisaatio'}
        label={i18n('kayttaja.organisaatio')}
        required={false}
        bind:model={kayttaja}
        lens={R.lensProp('organisaatio')}
        parse={R.trim}
        validators={schema.organisaatio}
        {disabled}
        {i18n} />
    </div>
  </div>

  <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Input
        id={'etunimi'}
        name={'etunimi'}
        label={i18n('kayttaja.etunimi')}
        required={true}
        bind:model={kayttaja}
        lens={R.lensProp('etunimi')}
        parse={R.trim}
        validators={schema.etunimi}
        {disabled}
        {i18n} />
    </div>
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Input
        id={'sukunimi'}
        name={'sukunimi'}
        label={i18n('kayttaja.sukunimi')}
        required={true}
        bind:model={kayttaja}
        lens={R.lensProp('sukunimi')}
        parse={R.trim}
        validators={schema.sukunimi}
        {disabled}
        {i18n} />
    </div>
  </div>
  <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4 items-end">
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Input
        id={'email'}
        name={'email'}
        label={`${i18n('kayttaja.email')}`}
        required={true}
        bind:model={kayttaja}
        lens={R.lensProp('email')}
        parse={R.trim}
        validators={schema.email}
        {disabled}
        {i18n} />
    </div>
    <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
      <Input
        id={'puhelin'}
        name={'puhelin'}
        label={i18n('kayttaja.puhelin')}
        required={false}
        bind:model={kayttaja}
        lens={R.lensProp('puhelin')}
        parse={R.trim}
        validators={schema.puhelin}
        {disabled}
        {i18n} />
    </div>
  </div>

  <H2 text={i18n('kayttaja.api-header')} />

  <div class="flex flex-col py-4">
    <ApiKey bind:dirty bind:kayttaja />
  </div>

  <H2 text={i18n('kayttaja.aineistot-header')} />

  <div>
    <table class="etp-table">
      <thead class="etp-table--thead">
        <tr class="etp-table--tr">
          <th class="etp-table--th" scope="col"
            >{i18n(i18nRoot + '.aineisto')}</th>
          <th class="etp-table--th" scope="col"
            >{i18n(i18nRoot + '.aineisto-lupa-paattymisaika')}</th>
          <th class="etp-table--th" scope="col"
            >{i18n(i18nRoot + '.aineisto-ip-osoite')}</th>
          <th class="etp-table--th" scope="col" />
        </tr>
      </thead>
      <tbody class="etp-table--tbody">
        {#each kayttajaAineistot as aineisto, index}
          <tr class="etp-table--tr">
            <td class="etp-table--td">
              <Select
                items={R.pluck('id', aineistot)}
                format={R.compose(
                  Maybe.orSome(''),
                  R.map(R.prop(`label-${Locales.shortLocale($locale)}`)),
                  Maybe.findById(R.__, aineistot)
                )}
                bind:model={kayttajaAineistot}
                lens={R.lensPath([index, 'aineisto-id'])} />
            </td>
            <td class="etp-table--td">
              <Datepicker
                id="valid-until"
                name="valid-until"
                bind:model={kayttajaAineistot}
                lens={R.lensPath([index, 'valid-until'])}
                format={Maybe.fold('', Formats.formatDateInstant)}
                parse={Parsers.optionalParser(Parsers.parseDate)}
                required={false}
                transform={EM.fromNull}
                {i18n} />
            </td>
            <td class="etp-table--td">
              <Input
                id="ip-address"
                name="ip-address"
                bind:model={kayttajaAineistot}
                lens={R.lensPath([index, 'ip-address'])}
                validators={schema['ip-address']}
                parse={R.trim}
                {i18n} />
            </td>
            <td class="etp-table--td etp-table--td__center">
              <button
                class="hover:bg-althover"
                title={i18n(`${i18nRoot}.aineisto-lupa-revoke`)}
                aria-label={`${i18n(`${i18nRoot}.aineisto-lupa-revoke`)} ${
                  aineisto['ip-address']
                }`}
                type="button"
                on:click={deleteAineisto(index)}>
                <span class="material-icons">delete_forever</span>
              </button>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
    <div class="mt-10">
      <Button
        on:click={addNewAineisto}
        disabled={maximumNumberOfIPsGiven}
        text={i18n(`${i18nRoot}.aineisto-lupa-grant`)}
        style={'secondary'} />
    </div>
  </div>

  <div class="flex -mx-4 mt-10">
    <div class="px-4">
      <Button
        type={'submit'}
        text={i18n('tallenna')}
        disabled={disabled || !dirty} />
    </div>
    <div class="px-4">
      <Button
        on:click={cancel}
        disabled={disabled || !dirty}
        text={i18n(i18nRoot + '.cancel')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
