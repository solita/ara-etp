import * as R from 'ramda';
import * as dfns from 'date-fns';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  DATE: 'DATE',
  BOOLEAN: 'BOOLEAN',
  VERSIO: 'VERSIO',
  ELUOKKA: 'ELUOKKA',
  VERSIOLUOKKA: 'VERSIOLUOKKA'
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
  serverCommand: 'like',
  format: R.curry((command, key, value) => [[command, key, `%${value}%`]])
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

const versioEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [[command, key, parseInt(value)]])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [2018],
  type: OPERATOR_TYPES.VERSIO
});

const versioluokkaEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, versio, luokka) => [
      ['=', 'versio', parseInt(versio)],
      [command, key, luokka]
    ])
  },
  key,
  argumentNumber: 1,
  defaultValues: () => [2018],
  type: OPERATOR_TYPES.VERSIOLUOKKA
});

const eLuokkaOperation = R.curry((operation, key) => ({
  operation,
  key,
  argumentNumber: 1,
  defaultValues: () => ['A'],
  type: OPERATOR_TYPES.ELUOKKA
}));

const eLuokkaEquals = eLuokkaOperation(eq);
const eLuokkaGreaterThan = eLuokkaOperation(eq);
const eLuokkaGreaterThanOrEqual = eLuokkaOperation(eq);
const eLuokkaLessThan = eLuokkaOperation(eq);
const eLuokkaLessThanOrEqual = eLuokkaOperation(eq);

const singleDateOperation = R.curry((dateGenerator, operation, key) => ({
  operation: {
    ...operation,
    format: R.curry((command, key, value) => [
      [command, key, R.compose(R.join('-'), R.reverse, R.split('.'))(value)]
    ])
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

const eLuokkaComparisons = [
  eLuokkaEquals,
  eLuokkaGreaterThan,
  eLuokkaGreaterThanOrEqual,
  eLuokkaLessThan,
  eLuokkaLessThanOrEqual
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
  kayttotarkoitus: [versioluokkaEquals],
  yritys: {
    nimi: [...stringComparisons]
  },
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
      osuuslampohaviosta: [...numberComparisons]
    },
    ylapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      osuuslampohaviosta: [...numberComparisons]
    },
    alapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      osuuslampohaviosta: [...numberComparisons]
    },
    ikkunat: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      osuuslampohaviosta: [...numberComparisons]
    },
    ulkoovet: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      osuuslampohaviosta: [...numberComparisons]
    },
    'kylmasillat-UA': [...numberComparisons],
    'kylmasillat-osuuslampohaviosta': [...numberComparisons]
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
  'e-luokka': [...eLuokkaComparisons],
  'kaytettavat-energiamuodot': {
    'fossiilinen-polttoaine': [...numberComparisons],
    'fossiilinen-polttoaine-painotettu-neliovuosikulutus': [
      ...numberComparisons
    ],
    sahko: [...numberComparisons],
    'sahko-painotettu-neliovuosikulutus': [...numberComparisons],
    kaukojaahdytys: [...numberComparisons],
    'kaukojaahdytys-painotettu-neliovuosikulutus': [...numberComparisons],
    kaukolampo: [...numberComparisons],
    'kaukolampo-painotettu-neliovuosikulutus': [...numberComparisons],
    'uusiutuva-polttoaine': [...numberComparisons],
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
    'aurinko-nelivuosikuorma': [...numberComparisons],
    ihmiset: [...numberComparisons],
    'ihmiset-nelivuosikuorma': [...numberComparisons],
    kuluttajalaitteet: [...numberComparisons],
    'kuluttajalaitteet-nelivuosikuorma': [...numberComparisons],
    valaistus: [...numberComparisons],
    'valaistus-nelivuosikuorma': [...numberComparisons],
    kvesi: [...numberComparisons],
    'kvesi-nelivuosikuorma': [...numberComparisons]
  },
  laskentatyokalu: [...stringComparisons]
};

const toteutunutOstoenergiankulutus = {
  'sahko-vuosikulutus-yhteensa': [...numberComparisons],
  'sahko-neliovuosikulutus-yhteensa': [...numberComparisons],
  'kaukolampo-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukolampo-neliovuosikulutus-yhteensa': [...numberComparisons],
  'kaukojaahdytys-vuosikulutus-yhteensa': [...numberComparisons],
  'kaukojaahdytys-neliovuosikulutus-yhteensa': [...numberComparisons],
  'polttoaineet-vuosikulutus-yhteensa': [...numberComparisons],
  'polttoaineet-neliovuosikulutus-yhteensa': [...numberComparisons],
  'toteutunut-vuosikulutus-yhteensä': [...numberComparisons],
  'toteutunut-neliovuosikulutus-yhteensä': [...numberComparisons],
  'ostetut-polttoaineet': {
    'kevyt-polttooljy-vuosikulutus': [...numberComparisons],
    'kevyt-polttooljy-neliovuosikulutus': [...numberComparisons]
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
  'tila-id': [...numberComparisons],
  perustiedot,
  lahtotiedot,
  tulokset,
  'toteutunut-ostoenergiankulutus': toteutunutOstoenergiankulutus,
  huomiot,
  versio: [versioEquals],
  'lisamerkintoja-fi': [stringContains],
  'lisamerkintoja-sv': [stringContains],
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
