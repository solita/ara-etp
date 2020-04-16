import * as validation from "@Utility/validation";

const String = max => [
  validation.liftValidator(validation.minLengthConstraint(2)),
  validation.liftValidator(validation.maxLengthConstraint(max))
];

const Rakennusvaippa = {
  ala: validation.MaybeInterval(0, 50),
  U: validation.MaybeInterval(0, 50),
}

const Ikkuna = {
  ala: validation.MaybeInterval(0, 9999),
  U: validation.MaybeInterval(0.4, 6.5),
  'g-ks': validation.MaybeInterval(0.1, 1.0)
}

export const schema = {
  perustiedot: {
    nimi: String(200),
    rakennustunnus: String(200),
    kiinteistotunnus: String(200),
    rakennusosa: String(200),
    'katuosoite-fi': String(200),
    'katuosoite-sv': String(200),
    postinumero: String(200),
    valmistumisvuosi: validation.MaybeInterval(100, new Date().getFullYear()),
    tilaaja: String(200),
    yritys: {
      nimi: String(200)
    },
    'keskeiset-suositukset-fi': String(200),
    'keskeiset-suositukset-sv': String(200),
  },
  lahtotiedot: {
    'lammitetty-nettoala': validation.MaybeInterval(0, 1000),
    rakennusvaippa: {
      ilmanvuotoluku: validation.MaybeInterval(0, 50),
      ulkoseinat: Rakennusvaippa,
      ylapohja: Rakennusvaippa,
      alapohja: Rakennusvaippa,
      ikkunat: Rakennusvaippa,
      ulkoovet: Rakennusvaippa,
      'kylmasillat-UA': validation.MaybeInterval(0, 50)
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
      'kuvaus-fi': String(500)
    }
  }
};