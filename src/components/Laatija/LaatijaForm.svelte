<script>
  import * as R from 'ramda';
  import * as dfns from 'date-fns';

  import { locale, _ } from '@Language/i18n';
  import * as LocaleUtils from '@Language/locale-utils';

  import H1 from '@Component/H/H1';
  import HR from '@Component/HR/HR';
  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox';
  import Autocomplete from '@Component/Autocomplete/Autocomplete';
  import Select from '@Component/Select/Select';
  import ToimintaalueetChecklist from '@Component/ToimintaalueetChecklist/ToimintaalueetChecklist';
  import * as laatijaApi from './laatija-api';
  import * as LaatijaUtils from './laatija-utils';

  import { currentUserStore, flashMessageStore } from '@/stores';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as country from '@Component/Geo/country-utils';
  import * as Validation from '@Utility/validation';
  import * as ToimintaAlueUtils from '@Component/Geo/toimintaalue-utils';
  import * as KayttajaUtils from '@Component/Kayttaja/kayttaja-utils';
  import * as Koodisto from '@Utility/koodisto';
  import * as formats from '@Utility/formats';

  const formParsers = LaatijaUtils.formParsers();
  const formSchema = LaatijaUtils.formSchema();

  export let laatija;
  export let luokittelut;
  export let submit;

  const originalLaatija = R.clone(laatija);

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

  $: laatija = R.over(
    R.lensProp('muuttoimintaalueet'),
    ToimintaAlueUtils.toimintaalueetWithoutMain(laatija.toimintaalue),
    laatija
  );

  $: formatLaskutuskieli = R.compose(
    Maybe.orSome(''),
    Maybe.map(labelLocale),
    Maybe.findById(R.__, luokittelut.laskutuskielet)
  );

  $: parseLaskutuskieli = R.identity;

  const isValidForm = R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    R.values,
    Validation.validateModelObject(formSchema)
  );

  const validateAndSubmit = _ => {
    if (isValidForm(laatija)) {
      flashMessageStore.flush();
      submit(laatija);
    } else {
      flashMessageStore.add(
        'Kayttaja',
        'error',
        $_('laatija.messages.validation-error')
      );
    }
  };
</script>

<style type="text/postcss">
  .lastlogin {
    @apply text-secondary mb-4;
  }
</style>

<form on:submit|preventDefault={validateAndSubmit}>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <span class="lastlogin">
      {R.compose( Maybe.orSome($_('kayttaja.no-login')), Maybe.map(R.concat($_('kayttaja.last-login') + ' ')), Maybe.map(formats.formatTimeInstant) )(laatija.login)}
    </span>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={$_('kayttaja.etunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('etunimi')}
          parse={formParsers.etunimi}
          validators={formSchema.etunimi}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={$_('kayttaja.sukunimi')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('sukunimi')}
          parse={formParsers.sukunimi}
          validators={formSchema.sukunimi}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'henkilotunnus'}
          name={'henkilotunnus'}
          label={$_('laatija.henkilotunnus')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('henkilotunnus')}
          parse={formParsers.henkilotunnus}
          validators={formSchema.henkilotunnus}
          disabled={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sahkoposti'}
          name={'sahkoposti'}
          label={`${$_('kayttaja.sahkoposti')}(${R.toLower($_('kayttaja.kayttajatunnus'))})`}
          required={true}
          disabled={true}
          bind:model={laatija}
          lens={R.lensProp('email')}
          parse={formParsers.email}
          validators={formSchema.email}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelinnumero'}
          name={'puhelinnumero'}
          label={$_('kayttaja.puhelinnumero')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('puhelin')}
          parse={formParsers.puhelin}
          validators={formSchema.puhelin}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'vastaanottajan-tarkenne'}
          name={'vastaanottajan-tarkenne'}
          label={$_('laatija.vastaanottajan-tarkenne')}
          required={false}
          bind:model={laatija}
          lens={R.lensProp('vastaanottajan-tarkenne')}
          format={Maybe.orSome('')}
          parse={formParsers['vastaanottajan-tarkenne']}
          validators={formSchema['vastaanottajan-tarkenne']}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'katuosoite'}
          name={'katuosoite'}
          label={$_('laatija.katuosoite')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('jakeluosoite')}
          parse={formParsers.jakeluosoite}
          validators={formSchema.jakeluosoite}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postinumero'}
          name={'postinumero'}
          label={$_('laatija.postinumero')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postinumero')}
          parse={formParsers.postinumero}
          validators={formSchema.postinumero}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postitoimipaikka'}
          name={'postitoimipaikka'}
          label={$_('laatija.postitoimipaikka')}
          required={true}
          bind:model={laatija}
          lens={R.lensProp('postitoimipaikka')}
          parse={formParsers.postitoimipaikka}
          validators={formSchema.postitoimipaikka}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Autocomplete items={countryNames}>
          <Input
            id={'maa'}
            name={'maa'}
            label={$_('laatija.maa')}
            required={true}
            bind:model={laatija}
            lens={R.lensProp('maa')}
            format={formatCountry}
            parse={parseCountry}
            search={true}
            handleSubmit={false}
            i18n={$_} />
        </Autocomplete>
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          label={$_('laatija.laskutuskieli')}
          format={formatLaskutuskieli}
          parse={parseLaskutuskieli}
          bind:model={laatija}
          lens={R.lensProp('laskutuskieli')}
          allowNone={false}
          items={laskutuskieletIds} />
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={$_('laatija.laatijatiedot')} />
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('laatimiskielto')}
          label={$_('laatija.todistustenlaatimiskielto')}
          disabled={R.compose( Maybe.getOrElse(true), R.map(R.compose( R.not, KayttajaUtils.kayttajaHasAccessToResource(
                  [2]
                ) )) )($currentUserStore)} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'patevyydenvoimassaolo'}
          name={'patevyydenvoimassaolo'}
          label={$_('laatija.patevyydenvoimassaolo')}
          bind:model={laatija}
          lens={R.lensProp('toteamispaivamaara')}
          format={toteamispaivamaara => `${dfns.format(dfns.parse(toteamispaivamaara, 'yyyy-M-d', 0), 'd.M.yyyy')} - ${dfns.format(dfns.add(
                dfns.parse(toteamispaivamaara, 'yyyy-M-d', 0),
                { years: 7 }
              ), 'd.M.yyyy')}`}
          parse={R.always(R.prop('toteamispaivamaara', laatija))}
          disabled={true}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Select
          label={$_('laatija.patevyystaso')}
          format={formatPatevyys}
          parse={parsePatevyys}
          bind:model={laatija}
          lens={R.lensProp('patevyystaso')}
          disabled={R.compose( Maybe.getOrElse(true), R.map(R.compose( R.not, KayttajaUtils.kayttajaHasAccessToResource(
                  [2]
                ) )) )($currentUserStore)}
          items={patevyydetIds} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4 flex flex-col">
        <Select
          label={$_('laatija.paatoimintaalue')}
          format={formatToimintaAlue}
          parse={parseToimintaAlue}
          bind:model={laatija}
          lens={R.lensProp('toimintaalue')}
          items={toimintaAlueetIds} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:py-0 w-full px-4 py-4 flex flex-col">
        <ToimintaalueetChecklist
          label={$_('laatija.muuttoimintaalueet')}
          toimintaalueet={toimintaAlueetIds}
          bind:model={laatija}
          lens={R.lensProp('muuttoimintaalueet')}
          format={formatToimintaAlue} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'wwwosoite'}
          name={'wwwosoite'}
          label={$_('laatija.www-osoite')}
          required={false}
          bind:model={laatija}
          lens={R.lensProp('wwwosoite')}
          format={Maybe.orSome('')}
          parse={formParsers.wwwosoite}
          validators={formSchema.wwwosoite}
          i18n={$_} />
      </div>
    </div>
  </div>
  <HR />
  <div class="mt-8">
    <H1 text={$_('laatija.julkisettiedot')} />
    <div class="flex flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 mb-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenpuhelin')}
          label={$_('kayttaja.puhelinnumero')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenemail')}
          label={$_('kayttaja.sahkoposti')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox
          bind:model={laatija}
          lens={R.lensProp('julkinenosoite')}
          label={$_('laatija.katuosoite')} />
      </div>
    </div>
  </div>
  <div class="flex -mx-4 mt-20">
    <div class="px-4">
      <Button type={'submit'} text={$_('tallenna')} />
    </div>
    <div class="px-4">
      <Button
        on:click={event => {
          event.preventDefault();
          laatija = R.clone(originalLaatija);
        }}
        text={$_('peruuta')}
        type={'reset'}
        style={'secondary'} />
    </div>
  </div>
</form>
