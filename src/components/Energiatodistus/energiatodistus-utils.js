import * as R from 'ramda';

import * as validation from '@Utility/validation';
import * as deep from '@Utility/deep-objects';
import * as Maybe from '@Utility/maybe-utils';
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

const emptyToimenpide = _ => ({
  'nimi-fi': Maybe.None(),
  'nimi-sv': Maybe.None(),
  lampo: Maybe.None(),
  sahko: Maybe.None(),
  jaahdytys: Maybe.None(),
  'eluvun-muutos': Maybe.None()
});

const emptyHuomio = _ => ({
  'teksti-fi': Maybe.None(),
  'teksti-sv': Maybe.None(),
  toimenpide: [emptyToimenpide(), emptyToimenpide(), emptyToimenpide()]
});

const emptySahkoLampo = _ => ({
  sahko: Maybe.None(),
  lampo: Maybe.None()
});

const emptySisKuorma = _ => ({
  kayttoaste: Maybe.None(),
  lampokuorma: Maybe.None()
});

const emptyVapaa = _ => ({
  nimi: Maybe.None(),
  yksikko: Maybe.None(),
  muunnoskerroin: Maybe.None(),
  'maara-vuodessa': Maybe.None()
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
    'keskeiset-suositukset-sv': Maybe.None()
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
      valokupu: emptyIkkuna(),
      katto: emptyIkkuna()
    },
    ilmanvaihto: {
      erillispoistot: emptyIV(),
      ivjarjestelma: emptyIV(),
      paaiv: R.mergeRight(
        {
          lampotilasuhde: Maybe.None(),
          jaatymisenesto: Maybe.None()
        },
        emptyIV()
      ),
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
    'sis-kuorma': {
      henkilot: emptySisKuorma(),
      kuluttajalaitteet: emptySisKuorma(),
      valaistus: emptySisKuorma()
    }
  },
  tulokset: {
    'kaytettavat-energiamuodot': {
      'fossiilinen-polttoaine': Maybe.None(),
      sahko: Maybe.None(),
      kaukojaahdytys: Maybe.None(),
      kaukolampo: Maybe.None(),
      'uusiutuva-polttoaine': Maybe.None()
    },
    'uusiutuvat-omavaraisenergiat': {
      aurinkosahko: Maybe.None(),
      tuulisahko: Maybe.None(),
      aurinkolampo: Maybe.None(),
      muulampo: Maybe.None(),
      muusahko: Maybe.None(),
      lampopumppu: Maybe.None()
    },
    'tekniset-jarjestelmat': {
      'tilojen-lammitys': emptySahkoLampo(),
      'tuloilman-lammitys': emptySahkoLampo(),
      'kayttoveden-valmistus': emptySahkoLampo(),
      'iv-sahko': Maybe.None(),
      jaahdytys: R.assoc('kaukojaahdytys', Maybe.None(), emptySahkoLampo()),
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
      vapaa: [emptyVapaa(), emptyVapaa(), emptyVapaa(), emptyVapaa()]
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
    yritys: {
      nimi: Maybe.None()
    }
  }
});

export const parsers = {
  optionalText: R.compose(Maybe.fromEmpty, R.trim)
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
  url: '/#/energiatodistus/all'
});

export const selectFormat = (label, items) =>
  R.compose(
    Either.cata(R.identity, R.identity),
    R.map(label),
    R.chain(Maybe.toEither('Unknown value')),
    R.map(R.__, items),
    Maybe.findById
  );

export const findKayttotarkoitusluokkaId = (
  alakayttotarkoitusluokkaId,
  alakayttotarkoitusluokat
) =>
  alakayttotarkoitusluokkaId.chain(
    R.compose(
      R.map(R.prop('kayttotarkoitusluokka-id')),
      Either.orSome(Maybe.None()),
      R.map(R.__, alakayttotarkoitusluokat),
      Maybe.findById
    )
  );

export const filterAlakayttotarkoitusLuokat = R.curry(
  (kayttotarkoitusluokkaId, alakayttotarkoitusluokat) =>
    alakayttotarkoitusluokat.map(
      R.filter(alaluokka =>
        Maybe.map(
          R.equals(alaluokka['kayttotarkoitusluokka-id']),
          kayttotarkoitusluokkaId
        ).orSome(true)
      )
    )
);

export const validators = deep.map(
  R.compose(R.complement(R.isNil), R.prop('validators')),
  R.prop('validators')
);

export const unnestValidation = R.compose(
  R.when(Either.isEither, Maybe.None),
  R.when(R.allPass([Either.isEither, Either.isRight]), Either.right)
);

export const energiatodistusPath = R.curry((path, et) =>
  R.compose(unnestValidation, R.path(path))(et)
);

export const calculatePaths = R.curry((calcFn, firstPath, secondPath, et) =>
  R.converge(R.lift(calcFn), [
    energiatodistusPath(firstPath),
    energiatodistusPath(secondPath)
  ])(et)
);

const rakennusvaippa = R.path(['lahtotiedot', 'rakennusvaippa']);

const fieldsWithUA = [
  'ulkoseinat',
  'ylapohja',
  'alapohja',
  'ikkunat',
  'ulkoovet'
];

export const rakennusvaippaUA = R.compose(
  R.converge(R.merge, [
    R.compose(R.map(unnestValidation), R.pick(['kylmasillat-UA'])),
    R.compose(
      R.map(
        R.compose(
          R.apply(R.lift(R.multiply)),
          R.map(unnestValidation),
          R.props(['ala', 'U'])
        )
      ),
      R.pick(fieldsWithUA)
    )
  ]),
  rakennusvaippa
);

const teknisetJarjestelmat = R.path(['tulokset', 'tekniset-jarjestelmat']);

const fieldsWithSahko = [
  'jaahdytys',
  'kayttoveden-valmistus',
  'tilojen-lammitys',
  'tuloilman-lammitys'
];

const fieldsWithLampo = [
  'jaahdytys',
  'kayttoveden-valmistus',
  'tilojen-lammitys',
  'tuloilman-lammitys'
];

export const teknistenJarjestelmienSahkot = R.compose(
  R.map(unnestValidation),
  R.converge(R.merge, [
    R.pick(['iv-sahko', 'kuluttajalaitteet-ja-valaistus-sahko']),
    R.compose(R.map(R.prop('sahko')), R.pick(fieldsWithSahko))
  ]),
  teknisetJarjestelmat
);

export const teknistenJarjestelmienLammot = R.compose(
  R.map(R.compose(unnestValidation, R.prop('lampo'))),
  R.pick(fieldsWithLampo),
  teknisetJarjestelmat
);

export const teknistenJarjestelmienKaukojaahdytys = R.compose(
  R.map(R.compose(unnestValidation, R.prop('kaukojaahdytys'))),
  R.pick(['jaahdytys']),
  teknisetJarjestelmat
);

export const sumEtValues = R.compose(
  R.reduce(R.lift(R.add), Maybe.of(0)),
  R.filter(Maybe.isSome),
  R.values
);

export const partOfSum = R.curry((sum, value) => R.lift(R.divide)(value, sum));

export const energiamuotokertoimet2018 = () => ({
  'fossiilinen-polttoaine': 1,
  kaukojaahdytys: 0.28,
  kaukolampo: 0.5,
  sahko: 1.2,
  'uusiutuva-polttoaine': 0.5
});

const fieldsWithErittelyOstoenergia = [
  'kaukolampo',
  'sahko',
  'uusiutuva-polttoaine',
  'fossiilinen-polttoaine',
  'kaukojaahdytys'
];

const kaytettavatEnergiamuodot = R.path([
  'tulokset',
  'kaytettavat-energiamuodot'
]);

export const ostoenergiat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithErittelyOstoenergia),
  kaytettavatEnergiamuodot
);

export const multiplyOstoenergia = R.curry((kerroin, ostoenergiamaara) =>
  R.map(R.multiply(kerroin), ostoenergiamaara)
);

export const perLammitettyNettoala = R.curry((energiatodistus, values) =>
  R.compose(
    R.map(R.__, values),
    R.lift(R.flip(R.divide)),
    energiatodistusPath(['lahtotiedot', 'lammitetty-nettoala'])
  )(energiatodistus)
);

const fieldsWithUusiutuvaOmavaraisenergia = [
  'aurinkosahko',
  'tuulisahko',
  'aurinkolampo',
  'muulampo',
  'muusahko',
  'lampopumppu'
];

const uusiutuvatOmavaraisenergiat = R.path([
  'tulokset',
  'uusiutuvat-omavaraisenergiat'
]);

export const omavaraisenergiat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithUusiutuvaOmavaraisenergia),
  uusiutuvatOmavaraisenergiat
);

const fieldsWithNettotarve = [
  'tilojen-lammitys-vuosikulutus',
  'ilmanvaihdon-lammitys-vuosikulutus',
  'kayttoveden-valmistus-vuosikulutus',
  'jaahdytys-vuosikulutus'
];

const nettotarve = R.path(['tulokset', 'nettotarve']);

export const nettotarpeet = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithNettotarve),
  nettotarve
);

const fieldsWithLampokuorma = [
  'aurinko',
  'ihmiset',
  'kuluttajalaitteet',
  'valaistus',
  'kvesi'
];

const lampokuormat = R.path(['tulokset', 'lampokuormat']);

export const kuormat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithLampokuorma),
  lampokuormat
);

const fieldsWithOstoenergia = [
  'kaukolampo-vuosikulutus',
  'kokonaissahko-vuosikulutus',
  'kiinteistosahko-vuosikulutus',
  'kayttajasahko-vuosikulutus',
  'kaukojaahdytys-vuosikulutus'
];

const ostettuEnergia = R.path([
  'toteutunut-ostoenergiankulutus',
  'ostettu-energia'
]);

export const ostetutEnergiamuodot = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithOstoenergia),
  ostettuEnergia
);
