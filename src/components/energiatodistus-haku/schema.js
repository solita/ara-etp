import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as EtUtils from '@Component/Energiatodistus/energiatodistus-utils';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  UNFORMATTED_NUMBER: 'UNFORMATTED_NUMBER',
  DATE: 'DATE',
  BOOLEAN: 'BOOLEAN',
  VERSIO: 'VERSIO',
  ELUOKKA: 'ELUOKKA',
  VERSIOLUOKKA: 'VERSIOLUOKKA',
  TILA: 'TILA',
  VERSIOKAYTTOTARKOITUSLUOKKA: 'VERSIOKAYTTOTARKOITUSLUOKKA',
  LAATIJA: 'LAATIJA'
});

const defaultFormat = R.curry((command, key, value) => [[command, key, value]]);

const eq = {
  browserCommand: '=',
  serverCommand: '=',
  format: defaultFormat
};
const gt = {
  browserCommand: '>',
  serverCommand: '>',
  format: defaultFormat
};
const gte = {
  browserCommand: '>=',
  serverCommand: '>=',
  format: defaultFormat
};
const lt = {
  browserCommand: '<',
  serverCommand: '<',
  format: defaultFormat
};
const lte = {
  browserCommand: '<=',
  serverCommand: '<=',
  format: defaultFormat
};

const contains = {
  browserCommand: 'sisaltaa',
  serverCommand: 'ilike',
  format: R.curry((command, key, value) => [[command, key, `%${value}%`]])
};

const some = {
  browserCommand: 'in',
  serverCommand: 'in',
  format: defaultFormat
};

const singleNumberOperation = R.curry((operation, type, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type
}));

const stringContains = key => ({
  operation: contains,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const stringEquals = key => ({
  operation: eq,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const versioEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [[command, key, parseInt(value)]])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.VERSIO
});

const versioluokkaEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, versio, luokka) => [
      ['=', 'versio', parseInt(versio)],
      [command, 'perustiedot.kayttotarkoitus', luokka]
    ])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [2018, ''],
  type: OPERATOR_TYPES.VERSIOLUOKKA
});

const versioluokkaSome = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, versio, luokat) => {
      return [
        ['=', 'versio', parseInt(versio)],
        ['in', key, R.split(',', luokat)]
      ];
    })
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [2018, ''],
  type: OPERATOR_TYPES.VERSIOKAYTTOTARKOITUSLUOKKA
});

const eLuokkaOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.ELUOKKA
}));

const eLuokkaSome = key => ({
  operation: some,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.ELUOKKA
});

const tilaOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => [EtUtils.tila.signed],
  type: OPERATOR_TYPES.TILA
}));

const tilaEquals = tilaOperation(eq);

const singleBoolean = key => ({
  operation: eq,
  key,
  argumentNumber: 1,
  defaultValues: () => [true],
  type: OPERATOR_TYPES.BOOLEAN
});

const uudisrakennusEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, uudisrakennus) => [
      ['=', 'versio', 2013],
      [command, 'perustiedot.uudisrakennus', uudisrakennus]
    ])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [true],
  type: OPERATOR_TYPES.BOOLEAN
});

const singleDateOperation = R.curry((operation, key) => ({
  operation: {
    ...operation,
    format: R.curry((command, key, value) => [
      [
        command,
        key,
        dfns
          .subMinutes(
            dfns.parseISO(value),
            dfns.parseISO(value).getTimezoneOffset()
          )
          .toISOString()
      ]
    ])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DATE
}));

const dateEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [
      [
        'between',
        key,
        dfns
          .subMinutes(
            dfns.parseISO(value),
            dfns.parseISO(value).getTimezoneOffset()
          )
          .toISOString(),
        dfns
          .subMinutes(
            dfns.addDays(dfns.parseJSON(value), 1),
            dfns.parseISO(value).getTimezoneOffset()
          )
          .toISOString()
      ]
    ])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DATE
});

const laatijaEquals = key => ({
  operation: eq,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.LAATIJA
});

const dateGreaterThan = singleDateOperation(gt);

const dateGreaterThanOrEqual = singleDateOperation(gte);

const dateLessThan = singleDateOperation(lt);

const dateLessThanOrEqual = singleDateOperation(lte);

const numberComparisonsFromType = type => [
  singleNumberOperation(eq, type),
  singleNumberOperation(gt, type),
  singleNumberOperation(gte, type),
  singleNumberOperation(lt, type),
  singleNumberOperation(lte, type)
];

const numberComparisons = numberComparisonsFromType(OPERATOR_TYPES.NUMBER);

const unformattedNumberComparisons = numberComparisonsFromType(
  OPERATOR_TYPES.UNFORMATTED_NUMBER
);

const dateComparisons = [
  dateEquals,
  dateGreaterThan,
  dateGreaterThanOrEqual,
  dateLessThan,
  dateLessThanOrEqual
];

const stringComparisons = [stringEquals, stringContains];

const eLuokkaComparisons = [eLuokkaSome];

const tilaComparisons = [tilaEquals];

const perustiedot = {
  nimi: [...stringComparisons],
  rakennustunnus: [...stringComparisons],
  kiinteistotunnus: [...stringComparisons],
  'julkinen-rakennus': [singleBoolean],
  uudisrakennus: [uudisrakennusEquals],
  'katuosoite-fi': [...stringComparisons],
  'katuosoite-sv': [...stringComparisons],
  postinumero: [...numberComparisons],
  laatimisvaihe: [...numberComparisons],
  valmistumisvuosi: [...unformattedNumberComparisons],
  tilaaja: [...stringComparisons],
  kayttotarkoitus: [versioluokkaSome],
  alakayttotarkoitusluokka: [versioluokkaEquals],
  yritys: {
    nimi: [...stringComparisons]
  },
  rakennusosa: [...stringComparisons],
  havainnointikaynti: [...dateComparisons],
  kieli: [...numberComparisons],
  'keskeiset-suositukset-fi': [stringContains],
  'keskeiset-suositukset-sv': [stringContains]
};

const lahtotiedot = {
  'lammitetty-nettoala': [...numberComparisons],
  rakennusvaippa: {
    ilmanvuotoluku: [...numberComparisons],
    ulkoseinat: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...numberComparisons],
      UA: [...numberComparisons]
    },
    ylapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...numberComparisons],
      UA: [...numberComparisons]
    },
    alapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...numberComparisons],
      UA: [...numberComparisons]
    },
    ikkunat: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...numberComparisons],
      UA: [...numberComparisons]
    },
    ulkoovet: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...numberComparisons],
      UA: [...numberComparisons]
    },
    'kylmasillat-UA': [...numberComparisons],
    'kylmasillat-osuus-lampohaviosta': [...numberComparisons]
  },
  ikkunat: {
    pohjoinen: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    koillinen: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    ita: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    kaakko: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    etela: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    lounas: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    lansi: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    },
    luode: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'g-ks': [...numberComparisons]
    }
  },
  ilmanvaihto: {
    'tyyppi-id': [...numberComparisons],
    'kuvaus-fi': [...stringComparisons],
    'kuvaus-sv': [...stringComparisons],
    paaiv: {
      poisto: [...numberComparisons],
      tulo: [...numberComparisons],
      sfp: [...numberComparisons],
      lampotilasuhde: [...numberComparisons],
      jaatymisenesto: [...numberComparisons]
    },
    erillispoistot: {
      tulo: [...numberComparisons],
      poisto: [...numberComparisons],
      sfp: [...numberComparisons]
    },
    'lto-vuosihyotysuhde': [...numberComparisons]
  },
  lammitys: {
    lammonjako: {
      id: [...numberComparisons],
      'kuvaus-fi': [...stringComparisons],
      'kuvaus-sv': [...stringComparisons]
    },
    'tilat-ja-iv': {
      'tuoton-hyotysuhde': [...numberComparisons],
      'jaon-hyotysuhde': [...numberComparisons],
      lampokerroin: [...numberComparisons],
      apulaitteet: [...numberComparisons],
      'lampohavio-lammittamaton-tila': [...numberComparisons],
      'lampopumppu-tuotto-osuus': [...numberComparisons]
    },
    'lammin-kayttovesi': {
      'tuoton-hyotysuhde': [...numberComparisons],
      'jaon-hyotysuhde': [...numberComparisons],
      lampokerroin: [...numberComparisons],
      apulaitteet: [...numberComparisons],
      'lampohavio-lammittamaton-tila': [...numberComparisons],
      'lampopumppu-tuotto-osuus': [...numberComparisons]
    },
    takka: { maara: [...numberComparisons], tuotto: [...numberComparisons] },
    ilmalampopumppu: {
      maara: [...numberComparisons],
      tuotto: [...numberComparisons]
    }
  },
  jaahdytysjarjestelma: {
    'jaahdytyskauden-painotettu-kylmakerroin': [...numberComparisons]
  },
  'lkvn-kaytto': {
    ominaiskulutus: [...numberComparisons],
    'lammitysenergian-nettotarve': [...numberComparisons]
  },
  'sis-kuorma': {
    henkilot: {
      kayttoaste: [...numberComparisons],
      lampokuorma: [...numberComparisons]
    },
    kuluttajalaitteet: {
      kayttoaste: [...numberComparisons],
      lampokuorma: [...numberComparisons]
    },
    valaistus: {
      kayttoaste: [...numberComparisons],
      lampokuorma: [...numberComparisons]
    }
  }
};

const tulokset = {
  'e-luku': [...numberComparisons],
  'e-luokka': [...eLuokkaComparisons],
  'kaytettavat-energiamuodot': {
    'fossiilinen-polttoaine': [...numberComparisons],
    'fossiilinen-polttoaine-painotettu': [...numberComparisons],
    'fossiilinen-polttoaine-painotettu-neliovuosikulutus': [
      ...numberComparisons
    ],
    sahko: [...numberComparisons],
    'sahko-painotettu': [...numberComparisons],
    'sahko-painotettu-neliovuosikulutus': [...numberComparisons],
    kaukojaahdytys: [...numberComparisons],
    'kaukojaahdytys-painotettu': [...numberComparisons],
    'kaukojaahdytys-painotettu-neliovuosikulutus': [...numberComparisons],
    kaukolampo: [...numberComparisons],
    'kaukolampo-painotettu': [...numberComparisons],
    'kaukolampo-painotettu-neliovuosikulutus': [...numberComparisons],
    'uusiutuva-polttoaine': [...numberComparisons],
    'uusiutuva-polttoaine-painotettu': [...numberComparisons],
    'uusiutuva-polttoaine-painotettu-neliovuosikulutus': [...numberComparisons]
  },
  'uusiutuvat-omavaraisenergiat': {
    aurinkosahko: [...numberComparisons],
    'aurinkosahko-neliovuosikulutus': [...numberComparisons],
    tuulisahko: [...numberComparisons],
    'tuulisahko-neliovuosikulutus': [...numberComparisons],
    aurinkolampo: [...numberComparisons],
    'aurinkolampo-neliovuosikulutus': [...numberComparisons],
    muulampo: [...numberComparisons],
    'muulampo-neliovuosikulutus': [...numberComparisons],
    muusahko: [...numberComparisons],
    'muusahko-neliovuosikulutus': [...numberComparisons],
    lampopumppu: [...numberComparisons],
    'lampopumppu-neliovuosikulutus': [...numberComparisons]
  },
  'tekniset-jarjestelmat': {
    'tilojen-lammitys': {
      sahko: [...numberComparisons],
      lampo: [...numberComparisons]
    },
    'tuloilman-lammitys': {
      sahko: [...numberComparisons],
      lampo: [...numberComparisons]
    },
    'kayttoveden-valmistus': {
      sahko: [...numberComparisons],
      lampo: [...numberComparisons]
    },
    'iv-sahko': [...numberComparisons],
    jaahdytys: {
      sahko: [...numberComparisons],
      kaukojaahdytys: [...numberComparisons]
    },
    'kuluttajalaitteet-ja-valaistus-sahko': [...numberComparisons],
    sahko: [...numberComparisons],
    lampo: [...numberComparisons],
    kaukojaahdytys: [...numberComparisons]
  },
  nettotarve: {
    'tilojen-lammitys-vuosikulutus': [...numberComparisons],
    'tilojen-lammitys-neliovuosikulutus': [...numberComparisons],
    'ilmanvaihdon-lammitys-vuosikulutus': [...numberComparisons],
    'ilmanvaihdon-lammitys-neliovuosikulutus': [...numberComparisons],
    'kayttoveden-valmistus-vuosikulutus': [...numberComparisons],
    'kayttoveden-valmistus-neliovuosikulutus': [...numberComparisons],
    'jaahdytys-vuosikulutus': [...numberComparisons],
    'jaahdytys-neliovuosikulutus': [...numberComparisons]
  },
  lampokuormat: {
    aurinko: [...numberComparisons],
    'aurinko-neliovuosikuorma': [...numberComparisons],
    ihmiset: [...numberComparisons],
    'ihmiset-neliovuosikuorma': [...numberComparisons],
    kuluttajalaitteet: [...numberComparisons],
    'kuluttajalaitteet-neliovuosikuorma': [...numberComparisons],
    valaistus: [...numberComparisons],
    'valaistus-neliovuosikuorma': [...numberComparisons],
    kvesi: [...numberComparisons],
    'kvesi-neliovuosikuorma': [...numberComparisons]
  },
  laskentatyokalu: [...stringComparisons]
};

const toteutunutOstoenergiankulutus = {
  'sahko-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukolampo-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukojaahdytys-vuosikulutus-yhteensa': [...numberComparisons],
  'polttoaineet-vuosikulutus-yhteensa': [...numberComparisons],
  'ostettu-energia': {
    'kaukojaahdytys-neliovuosikulutus': [...numberComparisons],
    'kaukolampo-neliovuosikulutus': [...numberComparisons],
    'kayttajasahko-neliovuosikulutus': [...numberComparisons],
    'kiinteistosahko-neliovuosikulutus': [...numberComparisons],
    'kokonaissahko-neliovuosikulutus': [...numberComparisons]
  },
  'ostetut-polttoaineet': {
    'kevyt-polttooljy-neliovuosikulutus': [...numberComparisons],
    'pilkkeet-havu-sekapuu-neliovuosikulutus': [...numberComparisons],
    'pilkkeet-koivu-neliovuosikulutus': [...numberComparisons],
    'puupelletit-neliovuosikulutus': [...numberComparisons]
  }
};

const huomiot = {
  'suositukset-fi': [...stringComparisons],
  'suositukset-sv': [...stringComparisons],
  'lisatietoja-fi': [...stringComparisons],
  'lisatietoja-sv': [...stringComparisons],
  'iv-ilmastointi': {
    'teksti-fi': [...stringComparisons],
    'teksti-sv': [...stringComparisons]
  },
  'valaistus-muut': {
    'teksti-fi': [...stringComparisons],
    'teksti-sv': [...stringComparisons]
  },
  lammitys: {
    'teksti-fi': [...stringComparisons],
    'teksti-sv': [...stringComparisons]
  },
  ymparys: {
    'teksti-fi': [...stringComparisons],
    'teksti-sv': [...stringComparisons]
  },
  'alapohja-ylapohja': {
    'teksti-fi': [...stringComparisons],
    'teksti-sv': [...stringComparisons]
  }
};

export const schema = {
  id: [...numberComparisons],
  'korvattu-energiatodistus-id': [...numberComparisons],
  allekirjoitusaika: [...dateComparisons],
  'tila-id': [...tilaComparisons],
  perustiedot,
  lahtotiedot,
  tulokset,
  'toteutunut-ostoenergiankulutus': toteutunutOstoenergiankulutus,
  huomiot,
  versio: [versioEquals],
  'lisamerkintoja-fi': [stringContains],
  'lisamerkintoja-sv': [stringContains],
  laskuriviviite: [...stringComparisons],
  'laatija-id': [laatijaEquals]
};

export const isOperationArray = R.compose(R.equals('Array'), R.type);

export const flattenSchema = R.curry((path, schema) => {
  const pairs = R.toPairs(schema);

  return R.reduce(
    (acc, obj) => {
      if (!isOperationArray(R.last(obj))) {
        return R.mergeRight(
          acc,
          flattenSchema(`${path}.${R.head(obj)}`, R.last(obj))
        );
      }

      const newPath = R.drop(1, `${path}.${R.head(obj)}`);

      return R.compose(
        R.assoc(newPath, R.__, acc),
        R.map(fn => fn(newPath)),
        R.last
      )(obj);
    },
    {},
    pairs
  );
});

const localizedField = key => [`${key}-fi`, `${key}-sv`];

export const laatijaSchema = R.compose(
  R.omit([
    'korvattu-energiatodistus-id',
    'perustiedot.kiinteistotunnus',
    'julkinen-rakennus',
    ...localizedField('perustiedot.keskeiset-suositukset'),
    ...localizedField('lisamerkintoja'),
    'lahtotiedot.rakennusvaippa.ilmanvuotoluku',
    'laatija-id',
    ''
  ]),
  flattenSchema(''),
  R.over(R.lensProp('lahtotiedot'), R.pick(['lammitetty-nettoala'])),
  R.over(R.lensProp('tulokset'), R.pick(['e-luku', 'e-luokka'])),
  R.dissoc('toteutunut-ostoenergiankulutus'),
  R.dissoc('huomiot')
)(schema);

export const paakayttajaSchema = R.compose(
  R.omit(['laskuriviviite']),
  flattenSchema('')
)(schema);

export const flatSchema = flattenSchema('', schema);
