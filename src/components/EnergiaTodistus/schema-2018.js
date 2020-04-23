import * as validation from '@Utility/validation';
import * as parsers from '@Utility/parsers';

const String = max => ({
  parse: parsers.optionalString,
  validators: [
    validation.liftValidator(validation.minLengthConstraint(2)),
    validation.liftValidator(validation.maxLengthConstraint(max))
  ]
});

const Integer = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseInteger),
  validators: validation.MaybeInterval(min, max)
});

const Float = (min, max) => ({
  parse: parsers.optionalParser(parsers.parseNumber),
  validators: validation.MaybeInterval(min, max)
});

const Rakennusvaippa = {
  ala: Float(0.0, 50),
  U: Float(0.0, 50)
};

const Ikkuna = {
  ala: Float(0.0, Infinity),
  U: Float(0.4, 6.5),
  'g-ks': Float(0.1, 1.0)
};

const PoistoTuloSfp = {
  poisto: Float(0.0, Infinity),
  tulo: Float(0.0, Infinity),
  sfp: Float(0.0, 10.0)
};

const Hyotysuhde = {
  'tuoton-hyotysuhde': Float(0.0, Infinity),
  'jaon-hyotysuhde': Float(0.0, Infinity),
  lampokerroin: Float(0.0, Infinity),
  apulaitteet: Float(0.0, Infinity)
};

const MaaraTuotto = {
  maara: Integer(0.0, 100),
  tuotto: Float(0.0, Infinity)
};

const SisKuorma = (minInclusive, maxInclusive) => ({
  kayttoaste: Float(0.1, 1.0),
  lampokuorma: Float(minInclusive, maxInclusive)
});

export const schema = {
  perustiedot: {
    nimi: String(200),
    rakennustunnus: String(200),
    kiinteistotunnus: String(200),
    rakennusosa: String(200),
    'katuosoite-fi': String(200),
    'katuosoite-sv': String(200),
    postinumero: String(200),
    valmistumisvuosi: Integer(100, new Date().getFullYear()),
    tilaaja: String(200),
    yritys: {
      nimi: String(200)
    },
    'keskeiset-suositukset-fi': String(200),
    'keskeiset-suositukset-sv': String(200)
  },
  lahtotiedot: {
    'lammitetty-nettoala': Float(0, 1000),
    rakennusvaippa: {
      ilmanvuotoluku: Float(0, 50),
      ulkoseinat: Rakennusvaippa,
      ylapohja: Rakennusvaippa,
      alapohja: Rakennusvaippa,
      ikkunat: Rakennusvaippa,
      ulkoovet: Rakennusvaippa,
      'kylmasillat-UA': Float(0, 50)
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
      'kulutus-per-nelio': Float(0.0, Infinity),
      vuosikulutus: Float(0.0, Infinity)
    },
    'sis-kuorma': {
      henkilot: SisKuorma(1.0, 14.0),
      kuluttajalaitteet: SisKuorma(0.0, 12.0),
      valaistus: SisKuorma(0.0, 19.0)
    }
  }
};
