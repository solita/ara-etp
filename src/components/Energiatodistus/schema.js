import * as validations from '@Utility/validation';
import * as parsers from '@Utility/parsers';
import * as formats from '@Utility/formats';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as Inputs from './inputs';
import * as Validation from './validation';
import * as Deep from '@Utility/deep-objects';

const String = max => ({
  parse: parsers.optionalString,
  format: formats.optionalString,
  validators: [
    validations.liftValidator(validations.minLengthConstraint(2)),
    validations.liftValidator(validations.maxLengthConstraint(max))
  ]
});

const Integer = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseInteger),
  format: formats.optionalNumber,
  deserialize: Either.Right,
  validators: validations.MaybeInterval(min, max)
});

const Float = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseNumber),
  format: formats.optionalNumber,
  deserialize: Either.Right,
  validators: validations.MaybeInterval(min, max)
});

const DateValue = () => ({
  parse: parsers.optionalParser(parsers.parseDate),
  format: R.compose(Maybe.orSome(''), R.map(formats.formatDateInstant)),
  deserialize: R.compose(parsers.toEitherMaybe, R.map(parsers.parseISODate)),
  serialize: R.map(
    R.map(date => dfns.formatISO(date, { representation: 'date' }))
  ),
  validators: []
});

const Rakennustunnus = {
  parse: R.compose(Maybe.fromEmpty, R.trim, R.toUpper),
  format: formats.optionalString,
  validators: [validations.liftValidator(validations.rakennustunnusValidator)]
};

const FloatNonNegative = Float(0.0, 9999999999);
const Float1 = Float(0.0, 1.0);
const AnyFloat = Float(-9999999999, 9999999999);
const IntegerNonNegative = Integer(0.0, 9999999999);

const Rakennusvaippa = {
  ala: FloatNonNegative,
  U: FloatNonNegative
};

const Ikkuna = {
  ala: FloatNonNegative,
  U: FloatNonNegative,
  'g-ks': Float1
};

const PoistoTuloSfp = {
  poisto: FloatNonNegative,
  tulo: FloatNonNegative,
  sfp: FloatNonNegative
};

const Hyotysuhde = {
  'tuoton-hyotysuhde': FloatNonNegative,
  'jaon-hyotysuhde': FloatNonNegative,
  lampokerroin: FloatNonNegative,
  apulaitteet: FloatNonNegative,
  'lampopumppu-tuotto-osuus': Float1,
  'lampohavio-lammittamaton-tila': FloatNonNegative
};

const MaaraTuotto = {
  maara: IntegerNonNegative,
  tuotto: FloatNonNegative
};

const SisKuorma = {
  kayttoaste: Float1,
  lampokuorma: FloatNonNegative
};

const SahkoLampo = {
  sahko: FloatNonNegative,
  lampo: FloatNonNegative
};

const MuuPolttoaine = {
  nimi: String(30),
  yksikko: String(12),
  muunnoskerroin: FloatNonNegative,
  'maara-vuodessa': FloatNonNegative
};

const Huomio = {
  teksti: String(1000),
  toimenpide: R.repeat(
    {
      nimi: String(100),
      lampo: AnyFloat,
      sahko: AnyFloat,
      jaahdytys: AnyFloat,
      'eluvun-muutos': AnyFloat
    },
    3
  )
};

const YritysPostinumero = String(8);

const Yritys = {
  nimi: String(40),
  katuosoite: String(40),
  postitoimipaikka: String(30),
  postinumero: YritysPostinumero
};

export const v2018 = {
  laskuriviviite: String(50),
  perustiedot: {
    nimi: String(50),
    rakennustunnus: Rakennustunnus,
    kiinteistotunnus: String(50),
    rakennusosa: String(100),
    katuosoite: String(50),
    postinumero: String(5),
    valmistumisvuosi: {
      ...Integer(100, new Date().getFullYear()),
      format: formats.optionalYear
    },
    tilaaja: String(200),
    yritys: Yritys,
    havainnointikaynti: DateValue(),
    'keskeiset-suositukset': String(2500)
  },
  lahtotiedot: {
    'lammitetty-nettoala': FloatNonNegative,
    rakennusvaippa: {
      ilmanvuotoluku: FloatNonNegative,
      lampokapasiteetti: FloatNonNegative,
      ilmatilavuus: FloatNonNegative,
      ulkoseinat: Rakennusvaippa,
      ylapohja: Rakennusvaippa,
      alapohja: Rakennusvaippa,
      ikkunat: Rakennusvaippa,
      ulkoovet: Rakennusvaippa,
      'kylmasillat-UA': FloatNonNegative
    },
    ikkunat: {
      pohjoinen: Ikkuna,
      koillinen: Ikkuna,
      ita: Ikkuna,
      kaakko: Ikkuna,
      etela: Ikkuna,
      lounas: Ikkuna,
      lansi: Ikkuna,
      luode: Ikkuna
    },
    ilmanvaihto: {
      kuvaus: String(75),
      paaiv: {
        ...PoistoTuloSfp,
        lampotilasuhde: Float1,
        jaatymisenesto: AnyFloat
      },
      erillispoistot: PoistoTuloSfp,
      ivjarjestelma: PoistoTuloSfp,
      'lto-vuosihyotysuhde': Float1,
      'tuloilma-lampotila': FloatNonNegative
    },
    lammitys: {
      'lammitysmuoto-1': {
        kuvaus: String(75)
      },
      'lammitysmuoto-2': {
        kuvaus: String(75)
      },
      lammonjako: {
        kuvaus: String(75)
      },
      'tilat-ja-iv': Hyotysuhde,
      'lammin-kayttovesi': Hyotysuhde,
      takka: MaaraTuotto,
      ilmalampopumppu: MaaraTuotto
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': FloatNonNegative
    },
    'lkvn-kaytto': {
      ominaiskulutus: FloatNonNegative,
      'lammitysenergian-nettotarve': FloatNonNegative
    },
    'sis-kuorma': {
      henkilot: SisKuorma,
      kuluttajalaitteet: SisKuorma,
      valaistus: SisKuorma
    }
  },
  tulokset: {
    'kaytettavat-energiamuodot': {
      'fossiilinen-polttoaine': FloatNonNegative,
      sahko: FloatNonNegative,
      kaukojaahdytys: FloatNonNegative,
      kaukolampo: FloatNonNegative,
      'uusiutuva-polttoaine': FloatNonNegative
    },
    'uusiutuvat-omavaraisenergiat': {
      aurinkosahko: FloatNonNegative,
      tuulisahko: FloatNonNegative,
      aurinkolampo: FloatNonNegative,
      muulampo: FloatNonNegative,
      muusahko: FloatNonNegative,
      lampopumppu: FloatNonNegative
    },
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': SahkoLampo,
      'tuloilman-lammitys': SahkoLampo,
      'kayttoveden-valmistus': SahkoLampo,
      'iv-sahko': FloatNonNegative,
      jaahdytys: { ...SahkoLampo, kaukojaahdytys: FloatNonNegative },
      'kuluttajalaitteet-ja-valaistus-sahko': FloatNonNegative
    },
    nettotarve: {
      'tilojen-lammitys-vuosikulutus': FloatNonNegative,
      'ilmanvaihdon-lammitys-vuosikulutus': FloatNonNegative,
      'kayttoveden-valmistus-vuosikulutus': FloatNonNegative,
      'jaahdytys-vuosikulutus': FloatNonNegative
    },
    lampokuormat: {
      aurinko: FloatNonNegative,
      ihmiset: FloatNonNegative,
      kuluttajalaitteet: FloatNonNegative,
      valaistus: FloatNonNegative,
      kvesi: FloatNonNegative
    },
    laskentatyokalu: String(60)
  },
  'toteutunut-ostoenergiankulutus': {
    'ostettu-energia': {
      'kaukolampo-vuosikulutus': FloatNonNegative,
      'kokonaissahko-vuosikulutus': FloatNonNegative,
      'kiinteistosahko-vuosikulutus': FloatNonNegative,
      'kayttajasahko-vuosikulutus': FloatNonNegative,
      'kaukojaahdytys-vuosikulutus': FloatNonNegative
    },
    'ostetut-polttoaineet': {
      'kevyt-polttooljy': FloatNonNegative,
      'pilkkeet-havu-sekapuu': FloatNonNegative,
      'pilkkeet-koivu': FloatNonNegative,
      puupelletit: FloatNonNegative,
      muu: R.repeat(MuuPolttoaine, 4)
    },
    'sahko-vuosikulutus-yhteensa': FloatNonNegative,
    'kaukolampo-vuosikulutus-yhteensa': FloatNonNegative,
    'polttoaineet-vuosikulutus-yhteensa': FloatNonNegative,
    'kaukojaahdytys-vuosikulutus-yhteensa': FloatNonNegative
  },
  huomiot: {
    suositukset: String(1500),
    lisatietoja: String(500),
    'iv-ilmastointi': Huomio,
    'valaistus-muut': Huomio,
    lammitys: Huomio,
    ymparys: Huomio,
    'alapohja-ylapohja': Huomio
  },
  lisamerkintoja: String(6300),
  kommentti: String(6300),
  'bypass-validation-limits-reason': String(6300)
};

const MuuEnergiamuoto = {
  nimi: String(30),
  muotokerroin: FloatNonNegative,
  ostoenergia: FloatNonNegative
};

const MuuEnergia = {
  nimi: String(30),
  vuosikulutus: FloatNonNegative
};

export const v2013 = R.compose(
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'muu'],
    R.repeat(MuuEnergiamuoto, 3)
  ),
  R.assocPath(
    ['tulokset', 'uusiutuvat-omavaraisenergiat'],
    R.repeat(MuuEnergia, 5)
  ),
  R.assocPath(
    ['toteutunut-ostoenergiankulutus', 'ostettu-energia', 'muu'],
    R.repeat(MuuEnergia, 5)
  ),
  R.dissocPath(['perustiedot', 'laatimisvaihe'])
)(v2018);

export const redefineNumericValidation = (schema, constraint) => {
  const path = R.append(R.__, R.split('.', constraint.property));
  return R.compose(
    R.assocPath(
      path('validators'),
      validations.MaybeInterval(constraint.error.min, constraint.error.max)
    ),
    R.assocPath(
      path('warnValidators'),
      validations.MaybeIntervalWarning(
        constraint.warning.min,
        constraint.warning.max
      )
    )
  )(schema);
};

export const assocRequired = (schema, property) => {
  const path = R.split('.', Inputs.removeLocalePostfix(property));
  const language = R.endsWith('-fi', property)
    ? 'fi'
    : R.endsWith('-sv', property)
    ? 'sv'
    : 'all';

  return R.unless(
    R.compose(R.isNil, R.path(path)),
    R.assocPath(
      R.concat(path, ['required', language]),
      Validation.predicate(property)
    ),
    schema
  );
};

export const appendRequiredValidators = (schema, isRequired) => Deep.map(
    R.propSatisfies(R.is(Array), 'validators'),
    R.when(
      R.has('required'),
      type => {
        const isRequiredPredicate = lang => R.defaultTo(
          type.required.all,
          type.required[lang]);

        return R.over(R.lensProp('validators'), R.append({
          predicate: v => !isRequired(isRequiredPredicate) || Maybe.isSome(v),
          label: R.applyTo('validation.required')
        }), type);
      }
    ),
  schema);

