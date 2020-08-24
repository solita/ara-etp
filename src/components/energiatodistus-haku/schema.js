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

const stringEquals = key => ({
  operation: eq,
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

const stringComparisons = [stringEquals, stringContains];

const perustiedot = {
  nimi: [...stringComparisons],
  rakennustunnus: [...stringComparisons],
  kiinteistotunnus: [...stringComparisons],
  rakennusosa: [...stringComparisons],
  'onko-julkinen-rakennus': [singleBoolean],
  katuosoite: [...stringComparisons],
  postinumero: [...stringComparisons],
  valmistumisvuosi: [...numberComparisons],
  tilaaja: [...stringComparisons],
  yritys: {
    nimi: [...stringComparisons],
    katuosoite: [...stringComparisons],
    postitoimipaikka: [...stringComparisons],
    postinumero: [...stringComparisons]
  },
  // havainnointikaynti: [...dateComparisons],
  'keskeiset-suositukset': [...stringComparisons]
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
      tulo: [...numberComparisons],
      sfp: [...numberComparisons]
    },
    ivjarjestelma: {
      poisto: [...numberComparisons],
      tulo: [...numberComparisons],
      sfp: [...numberComparisons]
    },
    'lto-vuosihyotysuhde': [...numberComparisons]
  },
  lammitys: {
    'kuvaus-fi': [...stringComparisons],
    'kuvaus-sv': [...stringComparisons],
    'tilat-ja-iv': {
      'tuoton-hyotysuhde': [...numberComparisons],
      'jaon-hyotysuhde': [...numberComparisons],
      lampokerroin: [...numberComparisons],
      apulaitteet: [...numberComparisons]
    },
    'lammin-kayttovesi': {
      'tuoton-hyotysuhde': [...numberComparisons],
      'jaon-hyotysuhde': [...numberComparisons],
      lampokerroin: [...numberComparisons],
      apulaitteet: [...numberComparisons]
    },
    takka: { maara: [...numberComparisons], tuotto: [...numberComparisons] },
    ilmanlampopumppu: {
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
      lampo: [...numberComparisons],
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
  'ostettu-energia': {
    'kaukolampo-vuosikulutus': [...numberComparisons],
    'kokonaissahko-vuosikulutus': [...numberComparisons],
    'kiinteistosahko-vuosikulutus': [...numberComparisons],
    'kayttajasahko-vuosikulutus': [...numberComparisons],
    'kaukojaahdytys-vuosikulutus': [...numberComparisons]
  },
  'ostetut-polttoaineet': {
    'kevyt-polttooljy': [...numberComparisons],
    'pilkkeet-havu-sekapuu': [...numberComparisons],
    'pilkkeet-koivu': [...numberComparisons],
    puupelletit: [...numberComparisons],
    muu: {
      nimi: [...stringComparisons],
      yksikko: [...stringComparisons],
      muunnoskerroin: [...numberComparisons],
      'maara-vuodessa': [...numberComparisons]
    }
  },
  'sahko-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukolampo-vuosikulutus-yhteensa': [...numberComparisons],
  'polttoaineet-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukojaahdytys-vuosikulutus-yhteensa': [...numberComparisons]
};

const huomiot = {
  suositukset: [...stringComparisons],
  lisatietoja: [...stringComparisons],
  'iv-ilmastointi': {
    teksti: [...stringComparisons],
    toimenpide: {
      nimi: [...stringComparisons],
      lampo: [...numberComparisons],
      sahko: [...numberComparisons],
      jaahdytys: [...numberComparisons],
      'eluvun-muutos': [...numberComparisons]
    }
  },
  'valaistus-muut': {
    teksti: [...stringComparisons],
    toimenpide: {
      nimi: [...stringComparisons],
      lampo: [...numberComparisons],
      sahko: [...numberComparisons],
      jaahdytys: [...numberComparisons],
      'eluvun-muutos': [...numberComparisons]
    }
  },
  lammitys: {
    teksti: [...stringComparisons],
    toimenpide: {
      nimi: [...stringComparisons],
      lampo: [...numberComparisons],
      sahko: [...numberComparisons],
      jaahdytys: [...numberComparisons],
      'eluvun-muutos': [...numberComparisons]
    }
  },
  ymparys: {
    teksti: [...stringComparisons],
    toimenpide: {
      nimi: [...stringComparisons],
      lampo: [...numberComparisons],
      sahko: [...numberComparisons],
      jaahdytys: [...numberComparisons],
      'eluvun-muutos': [...numberComparisons]
    }
  },
  'alapohja-ylapohja': {
    teksti: [...stringComparisons],
    toimenpide: {
      nimi: [...stringComparisons],
      lampo: [...numberComparisons],
      sahko: [...numberComparisons],
      jaahdytys: [...numberComparisons],
      'eluvun-muutos': [...numberComparisons]
    }
  }
};

export const schema = {
  id: [...numberComparisons],
  'korvattu-energiatodistus-id': [...numberComparisons],

  perustiedot,
  lahtotiedot,
  tulokset,
  'toteutunut-ostoenergiankulutus': toteutunutOstoenergiankulutus,
  huomiot,
  lisamerkintoja: [...stringComparisons]
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

export const laatijaSchema = R.compose(
  flattenSchema(''),
  R.pick([
    'id',
    'korvattu-energiatodistus-id',
    'onko-julkinen-rakennus',
    'perustiedot',
    'lahtotiedot',
    'tulokset',
    'toteutunut-ostoenergiankulutus',
    'huomiot',
    'lisamerkintoja'
  ])
)(schema);
