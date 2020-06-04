import * as validation from '@Utility/validation';
import * as parsers from '@Utility/parsers';
import * as formats from '@Utility/formats';
import * as R from 'ramda';

const String = max => ({
  parse: parsers.optionalString,
  format: formats.optionalString,
  validators: [
    validation.liftValidator(validation.minLengthConstraint(2)),
    validation.liftValidator(validation.maxLengthConstraint(max))
  ]
});

const Integer = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseInteger),
  format: formats.optionalNumber,
  validators: validation.MaybeInterval(min, max)
});

const Float = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseNumber),
  format: formats.optionalNumber,
  validators: validation.MaybeInterval(min, max)
});

const DateValue = () => ({
  parse: parsers.optionalParser(parsers.parseDate),
  validators: []
});

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
  apulaitteet: FloatPos
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

const VapaaPolttoaine = {
  nimi: String(30),
  yksikko: String(12),
  muunnoskerroin: FloatPos,
  'maara-vuodessa': FloatPos
};

const Huomio = {
  'teksti-fi': String(1000),
  'teksti-sv': String(1000),
  toimenpide: [
    {
      'nimi-fi': String(100),
      'nimi-sv': String(100),
      lampo: AnyFloat,
      sahko: AnyFloat,
      jaahdytys: AnyFloat,
      'eluvun-muutos': AnyFloat
    }
  ]
};

const YritysPostinumero = String(8);

const Yritys = {
  nimi: String(150),
  katuosoite: String(100),
  postitoimipaikka: String(30),
  postinumero: YritysPostinumero
};

export const v2018 = {
  perustiedot: {
    nimi: String(50),
    rakennustunnus: String(200),
    kiinteistotunnus: String(50),
    rakennusosa: String(100),
    'katuosoite-fi': String(100),
    'katuosoite-sv': String(100),
    postinumero: String(8),
    valmistumisvuosi: Integer(100, new Date().getFullYear()),
    tilaaja: String(200),
    yritys: Yritys,
    havainnointikaynti: DateValue(),
    'keskeiset-suositukset-fi': String(2500),
    'keskeiset-suositukset-sv': String(2500)
  },
  lahtotiedot: {
    'lammitetty-nettoala': FloatPos,
    rakennusvaippa: {
      ilmanvuotoluku: Float(0, 50),
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
      'lto-vuosihyotysuhde': Float(0.0, 1.0)
    },
    lammitys: {
      'kuvaus-fi': String(75),
      'kuvaus-sv': String(75),
      'tilat-ja-iv': Hyotysuhde,
      'lammin-kayttovesi': Hyotysuhde,
      takka: MaaraTuotto,
      ilmanlampopumppu: MaaraTuotto
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': Float(1.0, 10.0)
    },
    'lkvn-kaytto': {
      'kulutus-per-nelio': FloatPos,
      vuosikulutus: FloatPos
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
      vapaa: [
        VapaaPolttoaine,
        VapaaPolttoaine,
        VapaaPolttoaine,
        VapaaPolttoaine
      ]
    },
    'sahko-vuosikulutus-yhteensa': FloatPos,
    'kaukolampo-vuosikulutus-yhteensa': FloatPos,
    'polttoaineet-vuosikulutus-yhteensa': FloatPos,
    'kaukojaahdytys-vuosikulutus-yhteensa': FloatPos
  },
  huomiot: {
    'suositukset-fi': String(1500),
    'suositukset-sv': String(1500),
    'lisatietoja-fi': String(500),
    'lisatietoja-sv': String(500),
    'iv-ilmastointi': Huomio,
    'valaistus-muut': Huomio,
    lammitys: Huomio,
    ymparys: Huomio,
    'alapohja-ylapohja': Huomio
  },
  'lisamerkintoja-fi': String(6300),
  'lisamerkintoja-sv': String(6300)
};

const MuuEnergiamuoto = {
  nimi: String(30),
  muotokerroin: FloatPos,
  ostoenergia: FloatPos
};

export const v2013 = R.compose(
  R.assocPath(
    ['tulokset', 'kaytettavat-energiamuodot', 'muu'],
    R.repeat(MuuEnergiamuoto, 3)
  ),
  R.dissocPath(['perustiedot', 'laatimisvaihe'])
)(v2018);
