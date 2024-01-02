<script>
  import * as R from 'ramda';

  import { locale, _ } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Validation from '@Utility/validation';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as YritysUtils from './yritys-utils';
  import * as country from '@Utility/country';

  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import Select from '@Component/Select/Select';
  import Select2 from '@Component/Select/Select2';
  import H1 from '@Component/H/H1';
  import HR from '@Component/HR/HR';
  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import Confirm from '@Component/Confirm/Confirm';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';

  import { announcementsForModule } from '@Utility/announce';

  export let submit;
  export let cancel;
  export let setDeleted = Maybe.None();

  export let yritys;
  export let luokittelut;
  export let disabled = false;
  export let dirty = false;
  export let whoami;

  const setDirty = _ => {
    dirty = true;
  };
  const { announceError, clearAnnouncements } =
    announcementsForModule('yritys');

  $: schema = YritysUtils.schema(yritys.maa);

  const i18n = $_;
  const i18nRoot = 'yritys';

  $: labelLocale = LocaleUtils.label($locale);

  const parseCountry = R.compose(
    R.map(R.prop('id')),
    Maybe.toEither(R.applyTo('country-not-found')),
    country.findCountry(R.__, luokittelut.countries)
  );

  $: formatCountry = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    country.findCountryById(R.__, luokittelut.countries)
  );

  $: countryNames = R.map(labelLocale, luokittelut.countries);

  $: verkkolaskuoperaattoriNames = R.map(
    vlo => vlo.nimi + ' - ' + vlo.valittajatunnus,
    luokittelut.verkkolaskuoperaattorit
  );

  $: laskutuskieletIds = R.pluck('id', luokittelut.laskutuskielet);

  $: formatLaskutuskieli = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    Maybe.findById(R.__, luokittelut.laskutuskielet)
  );

  $: parseLaskutuskieli = R.identity;

  $: tyyppiIds = R.pluck('id', luokittelut.tyypit);

  $: formatTyyppi = R.compose(
    Maybe.orSome(''),
    R.map(labelLocale),
    Maybe.findById(R.__, luokittelut.tyypit)
  );

  const formatVerkkolaskuoperaattori = R.compose(
    Maybe.orSome(''),
    R.map(vlo => vlo.nimi + ' - ' + vlo.valittajatunnus),
    Maybe.findById(R.__, luokittelut.verkkolaskuoperaattorit)
  );

  const findVerkkolaskuoperaattori = R.curry((name, verkkolaskuoperaattorit) =>
    Maybe.find(
      R.compose(
        R.includes(R.compose(R.toLower, R.last, R.split(' - '))(name)),
        R.map(R.toLower),
        R.props(['valittajatunnus'])
      ),
      verkkolaskuoperaattorit
    )
  );

  const parseVerkkolaskuoperaattori = R.compose(
    Maybe.toEither(R.applyTo('validation.invalid-verkkolaskuoperaattori')),
    R.map(R.prop('id')),
    findVerkkolaskuoperaattori(R.__, luokittelut.verkkolaskuoperaattorit)
  );

  $: isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    Validation.validateModelObject(schema)
  );

  $: toggleDeletedKey = yritys.deleted ? 'undelete' : 'delete';
  $: perustiedotHeader =
    i18n(i18nRoot + '.perustiedot-header') +
    (yritys.deleted ? ` (${i18n(i18nRoot + '.deleted')})` : '');
</script>

<DirtyConfirmation {dirty} />
<form
  on:input={setDirty}
  on:change={setDirty}
  on:submit|preventDefault={event => {
    if (isValidForm(yritys)) {
      clearAnnouncements();
      submit(yritys);
    } else {
      Validation.blurForm(event.target);
      announceError(i18n('yritys.messages.validation-error'));
    }
  }}>
  <div class="w-full mt-3">
    <H1 text={perustiedotHeader} />
    <div class="flex lg:flex-row flex-col lg:py-4 -mx-4">
      {#if !R.isNil(yritys.id)}
        <div class="lg:w-1/6 lg:py-0 w-full px-4 py-4">
          <Input
            id={'id'}
            name={'id'}
            label={i18n('yritys.id')}
            required={true}
            bind:model={yritys}
            lens={R.lensProp('id')}
            {i18n}
            disabled="true" />
        </div>
      {/if}
      <div class="lg:w-2/6 lg:py-0 w-full px-4 py-4">
        <Input
          id={'ytunnus'}
          name={'ytunnus'}
          label={i18n('yritys.y-tunnus')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('ytunnus')}
          parse={R.trim}
          validators={schema.ytunnus}
          {i18n}
          {disabled} />
      </div>
      {#if Kayttajat.isPaakayttajaOrLaskuttaja(whoami)}
        <div class="lg:w-1/2 lg:py-0 w-full px-4 py-4">
          <Select2
            label={i18n('yritys.tyyppi')}
            required={true}
            {disabled}
            format={formatTyyppi}
            bind:model={yritys}
            lens={R.lensProp('type-id')}
            items={tyyppiIds} />
        </div>
      {/if}
    </div>
    <div class="flex lg:flex-row flex-col lg:py-4 -mx-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'nimi'}
          name={'nimi'}
          label={i18n('yritys.nimi')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('nimi')}
          parse={R.trim}
          validators={schema.nimi}
          {i18n}
          {disabled} />
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={i18n(i18nRoot + '.laskutusosoite')} />
    <div class="flex flex-col">
      <div class="py-4">
        <Input
          {disabled}
          id={'vastaanottajan-tarkenne'}
          name={'vastaanottajan-tarkenne'}
          label={i18n('yritys.vastaanottajan-tarkenne')}
          required={false}
          bind:model={yritys}
          lens={R.lensProp('vastaanottajan-tarkenne')}
          format={Maybe.orSome('')}
          parse={R.compose(Maybe.fromEmpty, R.trim)}
          validators={schema['vastaanottajan-tarkenne']}
          {i18n} />
      </div>
    </div>
    <div class="flex flex-col">
      <div class="py-4">
        <Input
          {disabled}
          id={'jakeluosoite'}
          name={'jakeluosoite'}
          label={i18n('yritys.jakeluosoite')}
          required={true}
          bind:model={yritys}
          lens={R.lensProp('jakeluosoite')}
          parse={R.trim}
          validators={schema.jakeluosoite}
          {i18n} />
      </div>
      <div class="flex lg:flex-row flex-col lg:py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            {disabled}
            id={'postinumero'}
            name={'postinumero'}
            label={i18n('yritys.postinumero')}
            required={true}
            bind:model={yritys}
            lens={R.lensProp('postinumero')}
            parse={R.trim}
            validators={schema.postinumero}
            {i18n} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Input
            {disabled}
            id={'postitoimipaikka'}
            name={'postitoimipaikka'}
            label={i18n('yritys.postitoimipaikka')}
            required={true}
            bind:model={yritys}
            lens={R.lensProp('postitoimipaikka')}
            parse={R.trim}
            validators={schema.postitoimipaikka}
            {i18n} />
        </div>
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Autocomplete items={countryNames}>
            <Input
              id={'maa'}
              name={'maa'}
              label={i18n('yritys.maa')}
              required={true}
              bind:model={yritys}
              lens={R.lensProp('maa')}
              format={formatCountry}
              parse={parseCountry}
              search={true}
              {i18n}
              {disabled} />
          </Autocomplete>
        </div>
      </div>
      <div class="flex lg:flex-row flex-col py-4 -mx-4">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Select
            label={i18n('yritys.laskutuskieli')}
            required={true}
            {disabled}
            format={formatLaskutuskieli}
            parse={parseLaskutuskieli}
            bind:model={yritys}
            lens={R.lensProp('laskutuskieli')}
            allowNone={false}
            items={laskutuskieletIds} />
        </div>
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={i18n('yritys.verkkolaskuosoite')} />
    <div class="lg:w-1/4 w-full">
      <Input
        {disabled}
        id={'verkkolaskuosoite'}
        name={'verkkolaskuosoite'}
        label={i18n('yritys.verkkolaskuosoite')}
        bind:model={yritys}
        lens={R.lensProp('verkkolaskuosoite')}
        format={Maybe.fold('', Formats.verkkolaskuosoite)}
        parse={YritysUtils.parseVerkkolaskuosoite}
        validators={schema['verkkolaskuosoite']}
        {i18n} />
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Autocomplete items={verkkolaskuoperaattoriNames} size={20}>
          <Input
            id={'verkkolaskuoperaattori'}
            name={'verkkolaskuoperaattori'}
            label={i18n('yritys.verkkolaskuoperaattori')}
            required={false}
            {disabled}
            format={Maybe.fold('', formatVerkkolaskuoperaattori)}
            parse={Parsers.optionalParser(parseVerkkolaskuoperaattori)}
            bind:model={yritys}
            lens={R.lensProp('verkkolaskuoperaattori')}
            search={true}
            {i18n} />
        </Autocomplete>
      </div>
    </div>
  </div>
  <div class="flex -mx-4 mt-20">
    <div class="px-4">
      <Button
        type={'submit'}
        text={i18n(i18nRoot + '.save-button')}
        disabled={disabled || !dirty} />
    </div>
    <div class="px-4">
      <Button
        on:click={cancel}
        text={i18n(i18nRoot + '.cancel-button')}
        type="button"
        style={'secondary'}
        disabled={disabled || !dirty} />
    </div>
    <div class="px-4">
      {#each Maybe.toArray(setDeleted) as update}
        <Confirm
          let:confirm
          confirmButtonLabel={i18n(`${i18nRoot}.${toggleDeletedKey}.button`)}
          confirmMessage={i18n(`${i18nRoot}.${toggleDeletedKey}.confirm`)}>
          <Button
            type="button"
            on:click={_ => confirm(_ => update(!yritys.deleted))}
            text={i18n(`${i18nRoot}.${toggleDeletedKey}.button`)}
            {disabled} />
        </Confirm>
      {/each}
    </div>
  </div>
</form>
