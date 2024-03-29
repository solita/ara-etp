<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as country from '@Utility/country';
  import * as Validation from '@Utility/validation';
  import * as ToimintaAlueUtils from '@Utility/toimintaalue';
  import * as Formats from '@Utility/formats';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as LocaleUtils from '@Language/locale-utils';

  import { locale, _ } from '@Language/i18n';

  import H2 from '@Component/H/H2';
  import HR from '@Component/HR/HR';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import Datepicker from '@Component/Input/Datepicker';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import Select from '@Component/Select/Select';
  import ToimintaalueetChecklist from './toimintaalueet-checklist/toimintaalueet-checklist';

  import * as LaatijaSchema from './schema';

  import { announcementsForModule } from '@Utility/announce';

  const i18n = $_;
  const i18nRoot = 'laatija';

  $: schema = LaatijaSchema.schema(laatija.maa);

  export let laatija;
  export let whoami;
  export let luokittelut;
  export let submit;
  export let dirty;
  export let cancel;
  export let errorModule = 'kayttaja';

  $: isPaakayttaja = Kayttajat.isPaakayttaja(whoami);
  $: isPartner = R.prop('partner', laatija);
  $: isOwnSettings = R.eqProps('id', laatija, whoami);

  $: disabled = !R.or(isPaakayttaja, isOwnSettings);

  $: labelLocale = LocaleUtils.label($locale);

  const parseCountry = R.compose(
    Either.map(R.prop('id')),
    Maybe.toEither(R.applyTo('country-not-found')),
    country.findCountry(R.__, luokittelut.countries)
  );

  $: formatCountry = R.compose(
    Maybe.orSome(''),
    Maybe.map(labelLocale),
    Maybe.findById(R.__, luokittelut.countries)
  );

  $: countryNames = R.map(labelLocale, luokittelut.countries);

  $: toimintaAlueetIds = R.pluck('id', luokittelut.toimintaalueet);

  $: patevyydetIds = R.pluck('id', luokittelut.patevyydet);

  $: laskutuskieletIds = R.pluck('id', luokittelut.laskutuskielet);

  $: formatPatevyys = R.compose(
    Maybe.orSome(''),
    Maybe.map(labelLocale),
    Maybe.findById(R.__, luokittelut.patevyydet)
  );

  $: parsePatevyys = R.identity;

  $: formatToimintaAlue = R.compose(
    Maybe.orSome(''),
    Maybe.map(labelLocale),
    Maybe.findById(R.__, luokittelut.toimintaalueet)
  );

  $: parseToimintaAlue = Maybe.fromNull;

  $: laatija = R.compose(
    R.when(
      R.compose(Maybe.isNone, R.prop('wwwosoite')),
      R.assoc('julkinenwwwosoite', false)
    ),
    R.over(
      R.lensProp('muuttoimintaalueet'),
      ToimintaAlueUtils.toimintaalueetWithoutMain(laatija.toimintaalue)
    )
  )(laatija);

  $: formatLaskutuskieli = R.compose(
    Maybe.orSome(''),
    Maybe.map(labelLocale),
    Maybe.findById(R.__, luokittelut.laskutuskielet)
  );

  $: parseLaskutuskieli = R.identity;

  let form;
  const validateAndSubmit = _ => {
    const { announceError, clearAnnouncements } =
      announcementsForModule(errorModule);

    if (Validation.isValidForm(schema)(laatija)) {
      clearAnnouncements();
      submit(laatija);
    } else {
      announceError(i18n('laatija.messages.validation-error'));
      Validation.blurForm(form);
    }
  };

  const formatPatevyydenVoimassaoloaika = laatija =>
    Maybe.fold(
      '-',
      Formats.formatDateInstant,
      Either.toMaybe(laatija.toteamispaivamaara)
    ) +
    ' - ' +
    Formats.inclusiveEndDate(laatija['voimassaolo-paattymisaika']);

  const setDirty = _ => {
    dirty = true;
  };
</script>

<form
  bind:this={form}
  on:submit|preventDefault={validateAndSubmit}
  on:input={setDirty}
  on:change={setDirty}>
  <div class="w-full mt-3">
    {#if isPaakayttaja}
      <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-2">
        <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
          <Checkbox
            bind:model={laatija}
            lens={R.lensProp('passivoitu')}
            label={i18n('kayttaja.passivoitu')}
            disabled={false} />
        </div>
        <div class="lg:w-1/3 w-full px-4">
          <Checkbox
            bind:model={laatija}
            lens={R.lensProp('partner')}
            label={i18n('laatija.partner')}
            disabled={true} />
        </div>
      </div>
    {/if}

    <H2 text={i18n(i18nRoot + '.perustiedot-header')} />
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={i18n('kayttaja.etunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('etunimi')}
          parse={R.trim}
          validators={schema.etunimi}
          disabled={!isPaakayttaja}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={i18n('kayttaja.sukunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('sukunimi')}
          parse={R.trim}
          validators={schema.sukunimi}
          disabled={!isPaakayttaja}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'henkilotunnus'}
          name={'henkilotunnus'}
          label={i18n('laatija.henkilotunnus')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('henkilotunnus')}
          format={Maybe.orSome('')}
          parse={R.compose(Maybe.fromEmpty, R.trim)}
          validators={schema.henkilotunnus}
          disabled={!isPaakayttaja || !isPartner}
          {i18n} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4 items-end">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'email'}
          name={'email'}
          label={`${i18n('kayttaja.email')} (${R.toLower(
            i18n('kayttaja.kayttajatunnus')
          )})`}
          required={true}
          {disabled}
          bind:model={laatija}
          lens={R.lensProp('email')}
          parse={R.trim}
          validators={schema.email}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelin'}
          name={'puhelin'}
          label={i18n('kayttaja.puhelin')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('puhelin')}
          parse={R.trim}
          validators={schema.puhelin}
          {disabled}
          {i18n} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'vastaanottajan-tarkenne'}
          name={'vastaanottajan-tarkenne'}
          label={i18n('laatija.vastaanottajan-tarkenne')}
          required={false}
          bind:model={laatija}
          lens={R.lensProp('vastaanottajan-tarkenne')}
          format={Maybe.orSome('')}
          parse={R.compose(Maybe.fromEmpty, R.trim)}
          validators={schema['vastaanottajan-tarkenne']}
          {disabled}
          {i18n} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'katuosoite'}
          name={'katuosoite'}
          label={i18n('laatija.katuosoite')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('jakeluosoite')}
          parse={R.trim}
          validators={schema.jakeluosoite}
          {disabled}
          {i18n} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postinumero'}
          name={'postinumero'}
          label={i18n('laatija.postinumero')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postinumero')}
          parse={R.trim}
          validators={schema.postinumero}
          {disabled}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postitoimipaikka'}
          name={'postitoimipaikka'}
          label={i18n('laatija.postitoimipaikka')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postitoimipaikka')}
          parse={R.trim}
          validators={schema.postitoimipaikka}
          {disabled}
          {i18n} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Autocomplete items={countryNames}>
          <Input
            id={'maa'}
            name={'maa'}
            label={i18n('laatija.maa')}
            required={true}
            bind:model={laatija}
            lens={R.lensProp('maa')}
            format={formatCountry}
            parse={parseCountry}
            search={true}
            handleSubmit={false}
            {disabled}
            {i18n} />
        </Autocomplete>
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          label={i18n('laatija.laskutuskieli')}
          required={true}
          format={formatLaskutuskieli}
          parse={parseLaskutuskieli}
          bind:model={laatija}
          lens={R.lensProp('laskutuskieli')}
          allowNone={false}
          {disabled}
          items={laskutuskieletIds} />
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H2 text={i18n('laatija.laatijatiedot-header')} />

    {#if R.or(laatija.laatimiskielto, isPaakayttaja)}
      <div class="flex lg:flex-row flex-col py-4 lg:-mx-4 my-4">
        <div class="lg:w-1/3 w-full px-4">
          <Checkbox
            bind:model={laatija}
            lens={R.lensProp('laatimiskielto')}
            label={i18n('laatija.todistustenlaatimiskielto')}
            disabled={!isPaakayttaja} />
        </div>
      </div>
    {/if}
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 w-full px-4">
        <Input
          id={'patevyydenvoimassaolo'}
          name={'patevyydenvoimassaolo'}
          label={i18n('laatija.patevyydenvoimassaolo')}
          bind:model={laatija}
          format={formatPatevyydenVoimassaoloaika}
          disabled={true}
          required={true}
          {i18n} />
      </div>
      {#if isPaakayttaja}
        <div class="md:w-1/3 w-full px-4 lg:py-0 py-4">
          <Datepicker
            id={'toteamispaivamaara'}
            name={'toteamispaivamaara'}
            label={i18n('laatija.toteamispaivamaara')}
            bind:model={laatija}
            lens={R.lensProp('toteamispaivamaara')}
            required={true}
            transform={Either.Right}
            {i18n} />
        </div>
      {/if}
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          label={i18n('laatija.patevyystaso')}
          format={formatPatevyys}
          parse={parsePatevyys}
          bind:model={laatija}
          lens={R.lensProp('patevyystaso')}
          disabled={!isPaakayttaja}
          items={patevyydetIds} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4 flex flex-col">
        <Select
          label={i18n('laatija.paatoimintaalue')}
          format={formatToimintaAlue}
          parse={parseToimintaAlue}
          bind:model={laatija}
          lens={R.lensProp('toimintaalue')}
          {disabled}
          items={toimintaAlueetIds} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div
        id="muuttoimintaalueet"
        class="lg:py-0 w-full px-4 py-4 flex flex-col">
        <ToimintaalueetChecklist
          label={i18n('laatija.muuttoimintaalueet')}
          toimintaalueet={toimintaAlueetIds}
          bind:model={laatija}
          lens={R.lensProp('muuttoimintaalueet')}
          {disabled}
          format={formatToimintaAlue} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'wwwosoite'}
          name={'wwwosoite'}
          label={i18n('laatija.www-osoite')}
          required={false}
          bind:model={laatija}
          lens={R.lensProp('wwwosoite')}
          format={Maybe.orSome('')}
          parse={LaatijaSchema.parseWWWOsoite}
          validators={schema.wwwosoite}
          {disabled}
          {i18n} />
      </div>
    </div>
  </div>

  <div class="mt-8">
    <H2 text={i18n('laatija.api-key-header')} />
    <div class="flex flex-col">
      <div class="lg:w-1/2 w-full">
        <Input
          id={'api-key'}
          name={'api-key'}
          label={i18n('laatija.api-key')}
          required={false}
          bind:model={laatija}
          lens={R.lensProp('api-key')}
          format={Maybe.orSome('')}
          parse={R.compose(Maybe.fromEmpty, R.trim)}
          validators={schema['api-key']}
          {disabled}
          {i18n} />
      </div>
      <div class="flex mt-4 items-center">
        <span class="font-icon mr-1 text-xl">info</span>
        <span>{i18n('laatija.api-key-requirements')}</span>
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H2 text={i18n('laatija.julkisettiedot-header')} />
    <div class="flex flex-col py-4 lg:-mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 mb-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenpuhelin')}
          {disabled}
          label={i18n('kayttaja.puhelin')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenemail')}
          {disabled}
          label={i18n('kayttaja.email')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenosoite')}
          {disabled}
          label={i18n('laatija.katuosoite')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenpostinumero')}
          {disabled}
          label={i18n('laatija.postinumero')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenwwwosoite')}
          disabled={disabled ||
            !R.compose(Maybe.isSome, R.prop('wwwosoite'))(laatija)}
          label={i18n('laatija.www-osoite')} />
      </div>
    </div>
  </div>
  <div class="flex lg:-mx-4 mt-20">
    {#if Kayttajat.isVerificationActive(whoami, laatija)}
      <div class="px-4">
        <Button
          prefix={'verify'}
          type={'submit'}
          text={i18n(i18nRoot + '.verify')}
          disabled={disabled || dirty} />
      </div>
    {/if}
    <div class="px-4">
      <Button
        prefix={'save'}
        type={'submit'}
        text={i18n(i18nRoot + '.save')}
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
