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

const stringEquals = key => ({
  operation: eq,
  key,
  argumentNumber: 1,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const singleDateOperation = R.curry((dateGenerator, operation, key) => ({
  operation: {
    ...operation,
    format: R.compose(R.join('-'), R.reverse, R.split('.'))
  },
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

const stringComparisons = [stringEquals, stringContains];

export const allOperations = [
  ...numberComparisons,
  ...dateComparisons,
  ...stringComparisons,
  singleBoolean
];

const perustiedot = {
  nimi: [...stringComparisons],
  rakennustunnus: [...stringComparisons],
  kiinteistotunnus: [...stringComparisons],
  'julkinen-rakennus': [singleBoolean],
  uudisrakennus: [singleBoolean],
  'katuosoite-fi': [...stringComparisons],
  'katuosoite-sv': [...stringComparisons],
  postinumero: [...stringComparisons],
  laatimisvaihe: [...numberComparisons],
  valmistumisvuosi: [...numberComparisons],
  tilaaja: [...stringComparisons],
  kayttotarkoitus: [...stringComparisons],
  yritys: {
    nimi: [...stringComparisons]
  },
  havainnointikaynti: [...dateComparisons],
  kieli: [...numberComparisons],
  'keskeiset-suositukset-fi': [...stringComparisons],
  'keskeiset-suositukset-sv': [...stringComparisons]
};

const lahtotiedot = {
  'lammitetty-nettoala': [...numberComparisons],
  rakennusvaippa: {
    ilmanvuotoluku: [...numberComparisons],
    ulkoseinat: {
      ala: [...numberComparisons],
      U: [...numberComparisons]
    },
    ylapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons]
    },
    alapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons]
    },
    ikkunat: {
      ala: [...numberComparisons],
      U: [...numberComparisons]
    },
    ulkoovet: {
      ala: [...numberComparisons],
      U: [...numberComparisons]
    },
    'kylmasillat-UA': [...numberComparisons]
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
  'e-luokka': [stringEquals],
  'kaytettavat-energiamuodot': {
    'fossiilinen-polttoaine': [...numberComparisons],
    sahko: [...numberComparisons],
    kaukojaahdytys: [...numberComparisons],
    kaukolampo: [...numberComparisons],
    'uusiutuva-polttoaine': [...numberComparisons]
  },
  'uusiutuvat-omavaraisenergiat': {
    aurinkosahko: [...numberComparisons],
    tuulisahko: [...numberComparisons],
    aurinkolampo: [...numberComparisons],
    muulampo: [...numberComparisons],
    muusahko: [...numberComparisons],
    lampopumppu: [...numberComparisons]
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
    'kuluttajalaitteet-ja-valaistus-sahko': [...numberComparisons]
  },
  nettotarve: {
    'tilojen-lammitys-vuosikulutus': [...numberComparisons],
    'ilmanvaihdon-lammitys-vuosikulutus': [...numberComparisons],
    'kayttoveden-valmistus-vuosikulutus': [...numberComparisons],
    'jaahdytys-vuosikulutus': [...numberComparisons]
  },
  lampokuormat: {
    aurinko: [...numberComparisons],
    ihmiset: [...numberComparisons],
    kuluttajalaitteet: [...numberComparisons],
    valaistus: [...numberComparisons],
    kvesi: [...numberComparisons]
  },
  laskentatyokalu: [...stringComparisons]
};

const toteutunutOstoenergiankulutus = {
  'sahko-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukolampo-vuosikulutus-yhteensa': [...numberComparisons],
  'polttoaineet-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukojaahdytys-vuosikulutus-yhteensa': [...numberComparisons]
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
  'tila-id': [...numberComparisons],
  perustiedot,
  lahtotiedot,
  tulokset,
  'toteutunut-ostoenergiankulutus': toteutunutOstoenergiankulutus,
  huomiot,
  versio: [...numberComparisons],
  'lisamerkintoja-fi': [...stringComparisons],
  'lisamerkintoja-sv': [...stringComparisons],
  laskuriviviite: [...stringComparisons],
  'laatija-fullname': [...stringComparisons]
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
