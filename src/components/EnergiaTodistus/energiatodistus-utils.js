import * as R from "ramda";

import * as validation from "@Utility/validation";
import * as deep from '@Utility/deep-objects';
import * as Maybe from "@Utility/maybe-utils";
import * as Either from '@Utility/either-utils';

const emptyRakennusVaippa = _ => ({
  ala: Maybe.None(),
  U: Maybe.None()
});

const emptyIkkuna = _ => ({
  ala: Maybe.None(),
  U: Maybe.None(),
  'g-ks': Maybe.None()
});

const emptyIV = _ => ({
  poisto: Maybe.None(),
  tulo: Maybe.None(),
  sfp: Maybe.None()
});

const emptyLammitys = _ => ({
  'tuoton-hyotysuhde': Maybe.None(),
  'jaon-hyotysuhde': Maybe.None(),
  lampokerroin: Maybe.None(),
  apulaitteet: Maybe.None()
});

const emptyLammitysMaaraTuotto = _ => ({
  maara: Maybe.None(),
  tuotto: Maybe.None()
});

const emptyHuomio = _ => ({
  'teksti-fi': Maybe.None(),
  'teksti-sv': Maybe.None(),
  toimenpide: []
});

const emptySahkoLampo = _ => ({
  sahko: Maybe.None(),
  lampo: Maybe.None()
});

export const emptyEnergiatodistus2018 = _ => ({
  perustiedot: {
    nimi: Maybe.None(),
    rakennustunnus: Maybe.None(),
    kiinteistotunnus: Maybe.None(),
    havainnointikaynti: Maybe.None(),
    kieli: Maybe.None(),
    rakennusosa: Maybe.None(),
    'katuosoite-fi': Maybe.None(),
    'katuosoite-sv': Maybe.None(),
    postinumero: Maybe.None(),
    valmistumisvuosi: Maybe.None(),
    'onko-julkinen-rakennus': false,
    tilaaja: Maybe.None(),
    yritys: {
      nimi: Maybe.None(),
      katuosoite: Maybe.None(),
      postinumero: Maybe.None(),
      postitoimipaikka: Maybe.None()
    },
    kayttotarkoitus: Maybe.None(),
    laatimisvaihe: Maybe.None(),
    'keskeiset-suositukset-fi': Maybe.None(),
    'keskeiset-suositukset-sv': Maybe.None(),
  },
  lahtotiedot: {
    'lammitetty-nettoala': Maybe.None(),
    rakennusvaippa: {
      ilmanvuotoluku: Maybe.None(),
      'kylmasillat-UA': Maybe.None(),
      ulkoseinat: emptyRakennusVaippa(),
      ylapohja: emptyRakennusVaippa(),
      alapohja: emptyRakennusVaippa(),
      ikkunat: emptyRakennusVaippa(),
      ulkoovet: emptyRakennusVaippa()
    },
    ikkunat: {
      pohjoinen: emptyIkkuna(),
      koillinen: emptyIkkuna(),
      ita: emptyIkkuna(),
      kaakko: emptyIkkuna(),
      etela: emptyIkkuna(),
      lounas: emptyIkkuna(),
      lansi: emptyIkkuna(),
      luode: emptyIkkuna(),
    },
    ilmanvaihto: {
      erillispoistot: emptyIV(),
      ivjarjestelma: emptyIV(),
      paaiv: R.mergeRight ({
        lampotilasuhde: Maybe.None(),
        jaatymisenesto: Maybe.None()
      }, emptyIV()),
      'kuvaus-fi': Maybe.None(),
      'kuvaus-sv': Maybe.None(),
      'lto-vuosihyotysuhde': Maybe.None()
    },
    lammitys: {
      'kuvaus-fi': Maybe.None(),
      'kuvaus-sv': Maybe.None(),
      'tilat-ja-iv': emptyLammitys(),
      'lammin-kayttovesi': emptyLammitys(),
      takka: emptyLammitysMaaraTuotto(),
      ilmanlampopumppu: emptyLammitysMaaraTuotto()
    },
    jaahdytysjarjestelma: {
      'jaahdytyskauden-painotettu-kylmakerroin': Maybe.None()
    },
    'lkvn-kaytto': {
      'kulutus-per-nelio': Maybe.None(),
      vuosikulutus: Maybe.None()
    },
    'sis-kuorma': []
  },
  tulokset: {
    'kaytettavat-energiamuodot': [],
    'uusiutuvat-omavaraisenergiat': [],
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': emptySahkoLampo(),
      'tuloilman-lammitys': emptySahkoLampo(),
      'kayttoveden-valmistus': Maybe.None(),
      'iv-sahko': Maybe.None(),
      jaahdytys: Maybe.None(),
      'kuluttajalaitteet-ja-valaistus-sahko': Maybe.None()
    },
    nettotarve: {
      'tilojen-lammitys-vuosikulutus': Maybe.None(),
      'ilmanvaihdon-lammitys-vuosikulutus': Maybe.None(),
      'kayttoveden-valmistus-vuosikulutus': Maybe.None(),
      'jaahdytys-vuosikulutus': Maybe.None()
    },
    lampokuormat: {
      aurinko: Maybe.None(),
      ihmiset: Maybe.None(),
      kuluttajalaitteet: Maybe.None(),
      valaistus: Maybe.None(),
      kvesi: Maybe.None()
    },
    laskentatyokalu: Maybe.None()
  },
  'toteutunut-ostoenergiankulutus': {
    'ostettu-energia': {
      'kaukolampo-vuosikulutus': Maybe.None(),
      'kokonaissahko-vuosikulutus': Maybe.None(),
      'kiinteistosahko-vuosikulutus': Maybe.None(),
      'kayttajasahko-vuosikulutus': Maybe.None(),
      'kaukojaahdytys-vuosikulutus': Maybe.None()
    },
    'ostetut-polttoaineet': {
      'kevyt-polttooljy': Maybe.None(),
      'pilkkeet-havu-sekapuu': Maybe.None(),
      'pilkkeet-koivu': Maybe.None(),
      puupelletit: Maybe.None(),
      vapaa: []
    },
    'sahko-vuosikulutus-yhteensa': Maybe.None(),
    'kaukolampo-vuosikulutus-yhteensa': Maybe.None(),
    'polttoaineet-vuosikulutus-yhteensa': Maybe.None(),
    'kaukojaahdytys-vuosikulutus-yhteensa': Maybe.None()
  },
  huomiot: {
    lammitys: emptyHuomio(),
    'alapohja-ylapohja': emptyHuomio(),
    'iv-ilmastointi': emptyHuomio(),
    ymparys: emptyHuomio(),
    'valaistus-muut': emptyHuomio(),

    'suositukset-fi': Maybe.None(),
    'suositukset-sv': Maybe.None(),
    'lisatietoja-fi': Maybe.None(),
    'lisatietoja-sv': Maybe.None()
  },
  'lisamerkintoja-fi': Maybe.None(),
  'lisamerkintoja-sv': Maybe.None()
});

export const emptyEnergiatodistus2013 = _ => ({
  perustiedot: {
    nimi: Maybe.None()
  }
});

export const parsers = {
  optionalText: R.compose(Maybe.fromEmpty, R.trim)
};

export const String = max => [
  validation.liftValidator(validation.minLengthConstraint(2)),
  validation.liftValidator(validation.maxLengthConstraint(max))
];

export const schema2018 = {
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
      ilmanvuotoluku: validation.MaybeInterval(0, 50)
    }
  }
};

export const schema2013 = {
  perustiedot: {
    nimi: [
      validation.liftValidator(validation.minLengthConstraint(2)),
      validation.liftValidator(validation.maxLengthConstraint(200))
    ]
  }
};

export const formatters = {
  optionalText: Maybe.orSome('')
};

export const isValidForm = R.compose(
  R.all(Either.isRight),
  R.filter(R.allPass([R.complement(R.isNil), Either.isEither])),
  deep.values(Either.isEither),
  validation.validateModelObject
);

export const breadcrumb1stLevel = i18n => ({
  label: i18n('energiatodistus.breadcrumb.energiatodistus'),
  url: '/#/energiatodistukset'
});

export const selectFormat = (label, items) => R.compose(
  Either.cata(R.identity, R.identity),
  R.map(label),
  R.chain(Maybe.toEither('Unknown value')),
  R.map(R.__, items),
  Maybe.findById
);

export const findKayttotarkoitusluokkaId = (alakayttotarkoitusluokkaId, alakayttotarkoitusluokat) =>
  alakayttotarkoitusluokkaId.chain(
    R.compose(
      R.map(R.prop('kayttotarkoitusluokka-id')),
      Either.orSome(Maybe.None()),
      R.map(R.__, alakayttotarkoitusluokat),
      Maybe.findById
    ));

export const filterAlakayttotarkoitusLuokat = R.curry(
  (kayttotarkoitusluokkaId, alakayttotarkoitusluokat) =>
    alakayttotarkoitusluokat.map(
      R.filter(alaluokka => Maybe.map(
        R.equals(alaluokka['kayttotarkoitusluokka-id']),
        kayttotarkoitusluokkaId).orSome(true))));