import * as validations from '@Utility/validation';
import * as parsers from '@Utility/parsers';
import * as formats from '@Utility/formats';
import * as R from 'ramda';

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
  validators: validations.MaybeInterval(min, max)
});

const Float = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseNumber),
  format: formats.optionalNumber,
  validators: validations.MaybeInterval(min, max)
});

const DateValue = () => ({
  parse: parsers.optionalParser(parsers.parseDate),
  validators: []
});

const StringValidator = validator => ({
  parse: parsers.optionalString,
  format: formats.optionalString,
  validators: [validator]
});

const Rakennustunnus = StringValidator(
  validations.liftValidator(validations.rakennustunnusValidator)
);

const FloatPos = Float(0.0, Infinity);

const AnyFloat = Float(-Infinity, Infinity);

const Rakennusvaippa = (min, max) => ({
  ala: FloatPos,
  U: Float(min, max)
});

const Ikkuna = {
  ala: FloatPos,
  U: Float(0.4, 6.5),
  'g-ks': Float(0.1, 1.0)
};

const PoistoTuloSfp = {
  poisto: FloatPos,
  tulo: FloatPos,
  sfp: Float(0.0, 10.0)
};

const Hyotysuhde = {
  'tuoton-hyotysuhde': FloatPos,
  'jaon-hyotysuhde': FloatPos,
  lampokerroin: FloatPos,
  apulaitteet: FloatPos,
  'lampopumppu-tuotto-osuus': Float(0.0, 1.0),
  'lampohavio-lammittamaton-tila': FloatPos
};

const MaaraTuotto = {
  maara: Integer(0, 100),
  tuotto: FloatPos
};

const SisKuorma = (minInclusive, maxInclusive) => ({
  kayttoaste: Float(0.1, 1.0),
  lampokuorma: Float(minInclusive, maxInclusive)
});
const SahkoLampo = {
  sahko: FloatPos,
  lampo: FloatPos
};

const MuuPolttoaine = {
  nimi: String(30),
  yksikko: String(12),
  muunnoskerroin: FloatPos,
  'maara-vuodessa': FloatPos
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
  nimi: String(150),
  katuosoite: String(100),
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
    katuosoite: String(100),
    postinumero: String(8),
    valmistumisvuosi: Integer(100, new Date().getFullYear()),
    tilaaja: String(200),
    yritys: Yritys,
    havainnointikaynti: DateValue(),
    'keskeiset-suositukset': String(2500)
  },
  lahtotiedot: {
    'lammitetty-nettoala': FloatPos,
    rakennusvaippa: {
      ilmanvuotoluku: Float(0, 50),
      lampokapasiteetti: FloatPos,
      ilmatilavuus: FloatPos,
      ulkoseinat: Rakennusvaippa(0.05, 2.0),
      ylapohja: Rakennusvaippa(0.03, 2.0),
      alapohja: Rakennusvaippa(0.03, 4.0),
      ikkunat: Rakennusvaippa(0.04, 6.5),
      ulkoovet: Rakennusvaippa(0.2, 6.5),
      'kylmasillat-UA': FloatPos
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
      'kuvaus-fi': String(75),
      'kuvaus-sv': String(75),
      paaiv: {
        ...PoistoTuloSfp,
        lampotilasuhde: Float(0.0, 1.0),
        jaatymisenesto: Float(-20.0, 10.0)
      },
      erillispoistot: PoistoTuloSfp,
      ivjarjestelma: PoistoTuloSfp,
      'lto-vuosihyotysuhde': Float(0.0, 1.0),
      'tuloilma-lampotila': FloatPos
    },
    lammitys: {
      'lammitysmuoto-1': {
        id: Integer(0, 9),
        kuvaus: String(75)
      },
      'lammitysmuoto-2': {
        id: Integer(0, 9),
        kuvaus: String(75)
      },
      lammonjako: {
        id: Integer(0, 12),
        kuvaus: String(75)
      },
      'tilat-ja-iv': Hyotysuhde,
      'lammin-kayttovesi': Hyotysuhde,
      takka: MaaraTuotto,
      ilmanlampopumppu: MaaraTuotto
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': Float(1.0, 10.0)
    },
    'lkvn-kaytto': {
      ominaiskulutus: FloatPos,
      'lammitysenergian-nettotarve': FloatPos
    },
    'sis-kuorma': {
      henkilot: SisKuorma(1.0, 14.0),
      kuluttajalaitteet: SisKuorma(0.0, 12.0),
      valaistus: SisKuorma(0.0, 19.0)
    }
  },
  tulokset: {
    'kaytettavat-energiamuodot': {
      'fossiilinen-polttoaine': FloatPos,
      sahko: FloatPos,
      kaukojaahdytys: FloatPos,
      kaukolampo: FloatPos,
      'uusiutuva-polttoaine': FloatPos
    },
    'uusiutuvat-omavaraisenergiat': {
      aurinkosahko: FloatPos,
      tuulisahko: FloatPos,
      aurinkolampo: FloatPos,
      muulampo: FloatPos,
      muusahko: FloatPos,
      lampopumppu: FloatPos
    },
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': SahkoLampo,
      'tuloilman-lammitys': SahkoLampo,
      'kayttoveden-valmistus': SahkoLampo,
      'iv-sahko': FloatPos,
      jaahdytys: { ...SahkoLampo, kaukojaahdytys: FloatPos },
      'kuluttajalaitteet-ja-valaistus-sahko': FloatPos
    },
    nettotarve: {
      'tilojen-lammitys-vuosikulutus': FloatPos,
      'ilmanvaihdon-lammitys-vuosikulutus': FloatPos,
      'kayttoveden-valmistus-vuosikulutus': FloatPos,
      'jaahdytys-vuosikulutus': FloatPos
    },
    lampokuormat: {
      aurinko: FloatPos,
      ihmiset: FloatPos,
      kuluttajalaitteet: FloatPos,
      valaistus: FloatPos,
      kvesi: FloatPos
    },
    laskentatyokalu: String(60)
  },
  'toteutunut-ostoenergiankulutus': {
    'ostettu-energia': {
      'kaukolampo-vuosikulutus': FloatPos,
      'kokonaissahko-vuosikulutus': FloatPos,
      'kiinteistosahko-vuosikulutus': FloatPos,
      'kayttajasahko-vuosikulutus': FloatPos,
      'kaukojaahdytys-vuosikulutus': FloatPos
    },
    'ostetut-polttoaineet': {
      'kevyt-polttooljy': FloatPos,
      'pilkkeet-havu-sekapuu': FloatPos,
      'pilkkeet-koivu': FloatPos,
      puupelletit: FloatPos,
      muu: R.repeat(MuuPolttoaine, 4)
    },
    'sahko-vuosikulutus-yhteensa': FloatPos,
    'kaukolampo-vuosikulutus-yhteensa': FloatPos,
    'polttoaineet-vuosikulutus-yhteensa': FloatPos,
    'kaukojaahdytys-vuosikulutus-yhteensa': FloatPos
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
  lisamerkintoja: String(6300)
};

const MuuEnergiamuoto = {
  nimi: String(30),
  muotokerroin: FloatPos,
  ostoenergia: FloatPos
};

const MuuEnergia = {
  nimi: String(30),
  vuosikulutus: FloatPos
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
      path('warningValidators'),
      validations.MaybeInterval(constraint.warning.min, constraint.warning.max)
    )
  )(schema);
};
