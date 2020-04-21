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
  ala: Float(0, 50),
  U: Float(0, 50)
};

const Ikkuna = {
  ala: Float(0, 9999),
  U: Float(0.4, 6.5),
  'g-ks': Float(0.1, 1.0)
};

const PoistoTulo = {
  poisto: Float(0, 9999),
  tulo: Float(0, 9999),
  sfp: Float(0.0, 10.0)
};

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
      'kuvaus-fi': String(500),
      paaiv: {
        ...PoistoTulo,
        lampotilasuhde: Float(0.0, 1.0),
        jaatymisenesto: Float(-20.0, 10.0)
      },
      erillispoistot: {
        ...PoistoTulo
      },
      ivjarjestelma: {
        ...PoistoTulo
      }
    }
  }
};
