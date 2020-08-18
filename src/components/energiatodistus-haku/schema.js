import * as R from 'ramda';
import * as dfns from 'date-fns';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  DATE: 'DATE',
  BOOLEAN: 'BOOLEAN'
});

const eq = {
  browserCommand: '=',
  serverCommand: '=',
  format: R.identity
};
const gt = {
  browserCommand: '>',
  serverCommand: '>',
  format: R.identity
};
const gte = {
  browserCommand: '>=',
  serverCommand: '>=',
  format: R.identity
};
const lt = {
  browserCommand: '<',
  serverCommand: '<',
  format: R.identity
};
const lte = {
  browserCommand: '<=',
  serverCommand: '<=',
  format: R.identity
};

const contains = {
  browserCommand: 'sisaltaa',
  serverCommand: 'like',
  format: arg => `%${arg}%`
};

const between = {
  browserCommand: 'valissa',
  serverCommand: 'between',
  format: R.identity
};

const singleNumberOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => [0],
  type: OPERATOR_TYPES.NUMBER
}));

const numberEquals = singleNumberOperation(eq);

const numberGreaterThan = singleNumberOperation(gt);

const numberGreaterThanOrEqual = singleNumberOperation(gte);

const numberLessThan = singleNumberOperation(lt);

const numberLessThanOrEqual = singleNumberOperation(lte);

const stringContains = key => ({
  operation: contains,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const singleDateOperation = R.curry((dateGenerator, operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => [dateGenerator()],
  type: OPERATOR_TYPES.DATE
}));

const singleBoolean = key => ({
  operation: eq,
  key,
  argumentNumber: 1,
  defaultValues: () => [true],
  type: OPERATOR_TYPES.BOOLEAN
});

const dateEquals = singleDateOperation(() => dfns.formatISO(new Date()), eq);

const dateGreaterThan = singleDateOperation(
  () => dfns.formatISO(new Date()),
  gt
);

const dateGreaterThanOrEqual = singleDateOperation(
  () => dfns.formatISO(new Date()),
  gte
);

const dateLessThan = singleDateOperation(() => dfns.formatISO(new Date()), lt);

const dateLessThanOrEqual = singleDateOperation(
  () => dfns.formatISO(new Date()),
  lte
);

const numberComparisons = [
  numberEquals,
  numberGreaterThan,
  numberGreaterThanOrEqual,
  numberLessThan,
  numberLessThanOrEqual
];

const dateComparisons = [
  dateEquals,
  dateGreaterThan,
  dateGreaterThanOrEqual,
  dateLessThan,
  dateLessThanOrEqual
];

const perustiedot = {
  nimi: {},
  rakennustunnus: {},
  kiinteistotunnus: {},
  rakennusosa: {},
  katuosoite: {},
  postinumero: {},
  valmistumisvuosi: {},
  tilaaja: {},
  yritys: {},
  havainnointikaynti: {},
  'keskeiset-suositukset': {}
};

const lahtotiedot = {
  'lammitetty-nettoala': {},
  rakennusvaippa: {
    ilmanvuotoluku: {},
    ulkoseinat: {},
    ylapohja: {},
    alapohja: {},
    ikkunat: {},
    ulkoovet: {},
    'kylmasillat-UA': {}
  },
  ikkunat: {
    pohjoinen: {},
    koillinen: {},
    ita: {},
    kaakko: {},
    etela: {},
    lounas: {},
    lansi: {},
    luode: {}
  },
  ilmanvaihto: {
    'kuvaus-fi': {},
    'kuvaus-sv': {},
    paaiv: {
      poisto: {},
      tulo: {},
      sfp: {},
      lampotilasuhde: {},
      jaatymisenesto: {}
    },
    erillispoistot: {},
    ivjarjestelma: {},
    'lto-vuosihyotysuhde': {}
  },
  lammitys: {
    'kuvaus-fi': {},
    'kuvaus-sv': {},
    'tilat-ja-iv': {},
    'lammin-kayttovesi': {},
    takka: {},
    ilmanlampopumppu: {}
  },
  jaahdytysjarjestelma: {
    'jaahdytyskauden-painotettu-kylmakerroin': {}
  },
  'lkvn-kaytto': {
    ominaiskulutus: {},
    'lammitysenergian-nettotarve': {}
  },
  'sis-kuorma': {
    henkilot: {},
    kuluttajalaitteet: {},
    valaistus: {}
  }
};

const tulokset = {
  'kaytettavat-energiamuodot': {
    'fossiilinen-polttoaine': {},
    sahko: {},
    kaukojaahdytys: {},
    kaukolampo: {},
    'uusiutuva-polttoaine': {}
  },
  'uusiutuvat-omavaraisenergiat': {
    aurinkosahko: {},
    tuulisahko: {},
    aurinkolampo: {},
    muulampo: {},
    muusahko: {},
    lampopumppu: {}
  },
  'tekniset-jarjestelmat': {
    'tilojen-lammitys': {},
    'tuloilman-lammitys': {},
    'kayttoveden-valmistus': {},
    'iv-sahko': {},
    jaahdytys: { sahko: {}, lampo: {}, kaukojaahdytys: {} },
    'kuluttajalaitteet-ja-valaistus-sahko': {}
  },
  nettotarve: {
    'tilojen-lammitys-vuosikulutus': {},
    'ilmanvaihdon-lammitys-vuosikulutus': {},
    'kayttoveden-valmistus-vuosikulutus': {},
    'jaahdytys-vuosikulutus': {}
  },
  lampokuormat: {
    aurinko: {},
    ihmiset: {},
    kuluttajalaitteet: {},
    valaistus: {},
    kvesi: {}
  },
  laskentatyokalu: {}
};

const toteutunutOstoenergiankulutus = {
  'ostettu-energia': {
    'kaukolampo-vuosikulutus': {},
    'kokonaissahko-vuosikulutus': {},
    'kiinteistosahko-vuosikulutus': {},
    'kayttajasahko-vuosikulutus': {},
    'kaukojaahdytys-vuosikulutus': {}
  },
  'ostetut-polttoaineet': {
    'kevyt-polttooljy': {},
    'pilkkeet-havu-sekapuu': {},
    'pilkkeet-koivu': {},
    puupelletit: {},
    muu: {
      nimi: {},
      yksikko: {},
      muunnoskerroin: {},
      'maara-vuodessa': {}
    }
  },
  'sahko-vuosikulutus-yhteensa': {},
  'kaukolampo-vuosikulutus-yhteensa': {},
  'polttoaineet-vuosikulutus-yhteensa': {},
  'kaukojaahdytys-vuosikulutus-yhteensa': {}
};

const huomiot = {
  suositukset: {},
  lisatietoja: {},
  'iv-ilmastointi': {},
  'valaistus-muut': {},
  lammitys: {},
  ymparys: {},
  'alapohja-ylapohja': {}
};

const schema = {
  id: R.map(R.applyTo('id'), numberComparisons),
  allekirjoitusaika: R.map(R.applyTo('allekirjoitusaika'), dateComparisons),
  'korvattu-energiatodistus-id': [
    stringContains('korvattu-energiatodistus-id')
  ],
  'perustiedot.onko-julkinen-rakennus': [
    singleBoolean('perustiedot.onko-julkinen-rakennus')
  ]
  // R.map(
  //   R.applyTo('korvattu-energiatodistus-id'),
  //   numberComparisons
  // )
};

export const laatijaSchema = R.pick(
  [
    'id',
    'allekirjoitusaika',
    'korvattu-energiatodistus-id',
    'perustiedot.onko-julkinen-rakennus'
  ],
  schema
);

export const isOperationArray = R.compose(R.equals('Array'), R.type);

export const flattenSchema = (path, schema) => {
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
};
