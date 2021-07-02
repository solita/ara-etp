import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as EtUtils from '@Pages/energiatodistus/energiatodistus-utils';
import * as DeepObjects from '@Utility/deep-objects';

export const OPERATOR_TYPES = Object.freeze({
  STRING: 'STRING',
  NUMBER: 'NUMBER',
  UNFORMATTED_NUMBER: 'UNFORMATTED_NUMBER',
  PERCENT: 'PERCENT',
  DATE: 'DATE',
  DATE_BETWEEN: 'DATE_BETWEEN',
  DAYCOUNT: 'DAYCOUNT',
  BOOLEAN: 'BOOLEAN',
  VERSIO: 'VERSIO',
  ELUOKKA: 'ELUOKKA',
  VERSIOLUOKKA: 'VERSIOLUOKKA',
  TILA: 'TILA',
  VERSIOKAYTTOTARKOITUSLUOKKA: 'VERSIOKAYTTOTARKOITUSLUOKKA',
  LAATIJA: 'LAATIJA',
  LAATIMISVAIHE: 'LAATIMISVAIHE',
  KIELISYYS: 'KIELISYYS',
  ILMANVAIHTOTYYPPI: 'ILMANVAIHTOTYYPPI',
  PATEVYYSTASO: 'PATEVYYSTASO'
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

const containsNo = {
  browserCommand: 'ei-sisalla',
  serverCommand: 'not ilike',
  format: R.curry((command, key, value) => [[command, key, `%${value}%`]])
};

const some = {
  browserCommand: 'in',
  serverCommand: 'in',
  format: defaultFormat
};

const between = {
  browserCommand: 'between',
  serverCommand: 'between',
  format: R.curry((command, key, startValue, endValue) => [
    [command, key, startValue, endValue]
  ])
};

const singleNumberOperation = R.curry((operation, type, key) => ({
  operation,
  key,
  defaultValues: () => [''],
  type
}));

const stringContains = key => ({
  operation: contains,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const stringContainsNo = key => ({
  operation: containsNo,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.STRING
});

const versioEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [[command, key, parseInt(value)]])
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.VERSIO
});

const versioluokkaEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, versio, luokka) => [
      ['=', 'energiatodistus.versio', parseInt(versio)],
      [command, 'energiatodistus.perustiedot.kayttotarkoitus', luokka]
    ])
  },
  key,
  defaultValues: () => [2018, ''],
  type: OPERATOR_TYPES.VERSIOLUOKKA
});

const versioluokkaSome = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, versio, luokat) => {
      return [
        ['=', 'energiatodistus.versio', parseInt(versio)],
        ['in', key, R.split(',', luokat)]
      ];
    })
  },
  key,
  defaultValues: () => [2018, ''],
  type: OPERATOR_TYPES.VERSIOKAYTTOTARKOITUSLUOKKA
});

const eLuokkaOperation = R.curry((operation, key) => ({
  operation,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.ELUOKKA
}));

const eLuokkaSome = key => ({
  operation: some,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.ELUOKKA
});

const tilaOperation = R.curry((operation, key) => ({
  operation,
  key,
  defaultValues: () => [EtUtils.tila.signed],
  type: OPERATOR_TYPES.TILA
}));

const tilaEquals = tilaOperation(eq);

const singleBoolean = key => ({
  operation: eq,
  key,
  defaultValues: () => [true],
  type: OPERATOR_TYPES.BOOLEAN
});

const uudisrakennusEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, uudisrakennus) => [
      ['=', 'energiatodistus.versio', 2013],
      [command, key, uudisrakennus]
    ])
  },
  key,
  defaultValues: () => [true],
  type: OPERATOR_TYPES.BOOLEAN
});

const laatijaEquals = key => ({
  operation: eq,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.LAATIJA
});

const laatijaPatevyys = key => ({
  operation: eq,
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.LAATIJA
});

const numberComparisonsFromType = type => [
  singleNumberOperation(eq, type),
  singleNumberOperation(gt, type),
  singleNumberOperation(gte, type),
  singleNumberOperation(lt, type),
  singleNumberOperation(lte, type)
];

const numberComparisonsWithoutEqFromType = type => [
  singleNumberOperation(gt, type),
  singleNumberOperation(gte, type),
  singleNumberOperation(lt, type),
  singleNumberOperation(lte, type)
];

const numberComparisons = numberComparisonsFromType(OPERATOR_TYPES.NUMBER);

const numberComparisonsWithoutEq = numberComparisonsWithoutEqFromType(
  OPERATOR_TYPES.NUMBER
);

const unformattedNumberComparisons = numberComparisonsFromType(
  OPERATOR_TYPES.UNFORMATTED_NUMBER
);

const percentComparisons = numberComparisonsFromType(OPERATOR_TYPES.PERCENT);

const stringComparisons = [stringContains, stringContainsNo];

const eLuokkaComparisons = [eLuokkaSome];

const tilaComparisons = [tilaEquals];

const luokitteluDefault = type => (type == OPERATOR_TYPES.PATEVYYSTASO ? 1 : 0);

const luokitteluEquals = R.curry((type, key) => ({
  operation: eq,
  key,
  defaultValues: () => [luokitteluDefault(type)],
  type
}));

const timeEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [
      ['between', key, value, dfns.endOfDay(dfns.parseISO(value)).toISOString()]
    ])
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DATE
});

const timeBetween = key => ({
  operation: {
    ...between,
    format: R.curry((command, key, startValue, endValue) => [
      [
        command,
        key,
        startValue,
        dfns.endOfDay(dfns.parseISO(endValue)).toISOString()
      ]
    ])
  },
  key,
  defaultValues: () => ['', ''],
  type: OPERATOR_TYPES.DATE_BETWEEN
});

const timeTodayAndDaysBefore = key => ({
  operation: {
    ...between,
    format: R.curry((command, key, numDays) => {
      const end = dfns.endOfDay(new Date());
      const start = dfns.startOfDay(dfns.subDays(end, numDays));
      return [[command, key, start.toISOString(), end.toISOString()]];
    }),
    browserCommand: 'kuluva-ja-edelliset'
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DAYCOUNT
});

const timeComparisons = [timeEquals, timeBetween, timeTodayAndDaysBefore];

const dateISOFormat = 'yyyy-MM-dd';

const dateEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [
      ['=', key, dfns.format(dfns.parseISO(value), dateISOFormat)]
    ])
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DATE
});

const dateBetween = key => ({
  operation: {
    ...between,
    format: R.curry((command, key, startValue, endValue) => [
      [
        command,
        key,
        dfns.format(dfns.parseISO(startValue), dateISOFormat),
        dfns.format(dfns.parseISO(endValue), dateISOFormat)
      ]
    ])
  },
  key,
  defaultValues: () => ['', ''],
  type: OPERATOR_TYPES.DATE_BETWEEN
});

const dateTodayAndDaysBefore = key => ({
  operation: {
    ...between,
    format: R.curry((command, key, numDays) => {
      const end = dfns.endOfDay(new Date());
      const start = dfns.startOfDay(dfns.subDays(end, numDays));
      return [
        [
          command,
          key,
          dfns.format(start, dateISOFormat),
          dfns.format(end, dateISOFormat)
        ]
      ];
    }),
    browserCommand: 'kuluva-ja-edelliset'
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DAYCOUNT
});

const dateComparisons = [dateEquals, dateBetween, dateTodayAndDaysBefore];

const havainnointikayntiEquals = key => ({
  operation: {
    ...eq,
    format: R.curry((command, key, value) => [
      ['=', 'perustiedot.laatimisvaihe', 2],
      [
        command,
        key,
        dfns.formatISO(dfns.parseISO(value), { representation: 'date' })
      ]
    ])
  },
  key,
  defaultValues: () => [''],
  type: OPERATOR_TYPES.DATE
});

const havainnointikayntiBetween = key => ({
  operation: {
    ...between,
    format: R.curry((command, key, startValue, endValue) => [
      ['=', 'perustiedot.laatimisvaihe', 2],
      [
        command,
        key,
        dfns.formatISO(dfns.parseISO(startValue), { representation: 'date' }),
        dfns.formatISO(dfns.parseISO(endValue), { representation: 'date' })
      ]
    ])
  },
  key,
  defaultValues: () => ['', ''],
  type: OPERATOR_TYPES.DATE_BETWEEN
});

const perustiedot = {
  nimi: [...stringComparisons],
  rakennustunnus: [...stringComparisons],
  kiinteistotunnus: [...stringComparisons],
  'julkinen-rakennus': [singleBoolean],
  uudisrakennus: [uudisrakennusEquals],
  'katuosoite-fi': [...stringComparisons],
  'katuosoite-sv': [...stringComparisons],
  postinumero: [...numberComparisons],
  laatimisvaihe: [luokitteluEquals(OPERATOR_TYPES.LAATIMISVAIHE)],
  valmistumisvuosi: [...unformattedNumberComparisons],
  tilaaja: [...stringComparisons],
  kayttotarkoitus: [versioluokkaSome],
  alakayttotarkoitusluokka: [versioluokkaEquals],
  yritys: {
    nimi: [...stringComparisons]
  },
  rakennusosa: [...stringComparisons],
  havainnointikaynti: [havainnointikayntiEquals, havainnointikayntiBetween],
  kieli: [luokitteluEquals(OPERATOR_TYPES.KIELISYYS)],
  'keskeiset-suositukset-fi': [...stringComparisons],
  'keskeiset-suositukset-sv': [...stringComparisons]
};

const lahtotiedot = {
  'lammitetty-nettoala': [...numberComparisons],
  rakennusvaippa: {
    ilmanvuotoluku: [...numberComparisons],
    ulkoseinat: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...percentComparisons],
      UA: [...numberComparisons]
    },
    ylapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...percentComparisons],
      UA: [...numberComparisons]
    },
    alapohja: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...percentComparisons],
      UA: [...numberComparisons]
    },
    ikkunat: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...percentComparisons],
      UA: [...numberComparisons]
    },
    ulkoovet: {
      ala: [...numberComparisons],
      U: [...numberComparisons],
      'osuus-lampohaviosta': [...percentComparisons],
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
    'tyyppi-id': [luokitteluEquals(OPERATOR_TYPES.ILMANVAIHTOTYYPPI)],
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
      ...numberComparisonsWithoutEq
    ],
    sahko: [...numberComparisons],
    'sahko-painotettu': [...numberComparisons],
    'sahko-painotettu-neliovuosikulutus': [...numberComparisonsWithoutEq],
    kaukojaahdytys: [...numberComparisons],
    'kaukojaahdytys-painotettu': [...numberComparisons],
    'kaukojaahdytys-painotettu-neliovuosikulutus': [
      ...numberComparisonsWithoutEq
    ],
    kaukolampo: [...numberComparisons],
    'kaukolampo-painotettu': [...numberComparisons],
    'kaukolampo-painotettu-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'uusiutuva-polttoaine': [...numberComparisons],
    'uusiutuva-polttoaine-painotettu': [...numberComparisons],
    'uusiutuva-polttoaine-painotettu-neliovuosikulutus': [
      ...numberComparisonsWithoutEq
    ]
  },
  'uusiutuvat-omavaraisenergiat': {
    aurinkosahko: [...numberComparisons],
    'aurinkosahko-neliovuosikulutus': [...numberComparisonsWithoutEq],
    tuulisahko: [...numberComparisons],
    'tuulisahko-neliovuosikulutus': [...numberComparisonsWithoutEq],
    aurinkolampo: [...numberComparisons],
    'aurinkolampo-neliovuosikulutus': [...numberComparisonsWithoutEq],
    muulampo: [...numberComparisons],
    'muulampo-neliovuosikulutus': [...numberComparisonsWithoutEq],
    muusahko: [...numberComparisons],
    'muusahko-neliovuosikulutus': [...numberComparisonsWithoutEq],
    lampopumppu: [...numberComparisons],
    'lampopumppu-neliovuosikulutus': [...numberComparisonsWithoutEq]
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
    'tilojen-lammitys-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'ilmanvaihdon-lammitys-vuosikulutus': [...numberComparisons],
    'ilmanvaihdon-lammitys-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'kayttoveden-valmistus-vuosikulutus': [...numberComparisons],
    'kayttoveden-valmistus-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'jaahdytys-vuosikulutus': [...numberComparisons],
    'jaahdytys-neliovuosikulutus': [...numberComparisonsWithoutEq]
  },
  lampokuormat: {
    aurinko: [...numberComparisons],
    'aurinko-neliovuosikuorma': [...numberComparisonsWithoutEq],
    ihmiset: [...numberComparisons],
    'ihmiset-neliovuosikuorma': [...numberComparisonsWithoutEq],
    kuluttajalaitteet: [...numberComparisons],
    'kuluttajalaitteet-neliovuosikuorma': [...numberComparisonsWithoutEq],
    valaistus: [...numberComparisons],
    'valaistus-neliovuosikuorma': [...numberComparisonsWithoutEq],
    kvesi: [...numberComparisons],
    'kvesi-neliovuosikuorma': [...numberComparisonsWithoutEq]
  },
  laskentatyokalu: [...stringComparisons]
};

const toteutunutOstoenergiankulutus = {
  'ostettu-energia': {
    'kaukojaahdytys-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'kaukolampo-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'kayttajasahko-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'kiinteistosahko-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'kokonaissahko-neliovuosikulutus': [...numberComparisonsWithoutEq]
  },
  'ostetut-polttoaineet': {
    'kevyt-polttooljy-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'pilkkeet-havu-sekapuu-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'pilkkeet-koivu-neliovuosikulutus': [...numberComparisonsWithoutEq],
    'puupelletit-neliovuosikulutus': [...numberComparisonsWithoutEq]
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

const laatija = {
  patevyystaso: [luokitteluEquals(OPERATOR_TYPES.PATEVYYSTASO)],
  toteamispaivamaara: dateComparisons,
  'voimassaolo-paattymisaika': timeComparisons
};

export const flattenSchema = R.compose(
  R.reduce((acc, arr) => ({ ...acc, [arr[0].key]: arr }), {}),
  R.map(R.converge(R.map, [R.compose(R.applyTo, R.head), R.last])),
  R.toPairs,
  DeepObjects.treeFlat('.', R.is(Array))
);

export const schema = {
  energiatodistus: {
    id: [...numberComparisons],
    'korvattu-energiatodistus-id': [...numberComparisons],
    allekirjoitusaika: [...timeComparisons],
    'voimassaolo-paattymisaika': [...timeComparisons],
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
  },
  laatija
};

const localizedField = key => [`${key}-fi`, `${key}-sv`];

export const laatijaSchema = R.compose(
  R.omit([
    'energiatodistus.korvattu-energiatodistus-id',
    'energiatodistus.perustiedot.kiinteistotunnus',
    'energiatodistus.julkinen-rakennus',
    ...localizedField('energiatodistus.perustiedot.keskeiset-suositukset'),
    ...localizedField('energiatodistus.lisamerkintoja'),
    'energiatodistus.lahtotiedot.rakennusvaippa.ilmanvuotoluku',
    'energiatodistus.laatija-id',
    'laatija.patevyystaso',
    'laatija.toteamispaivamaara'
  ]),
  flattenSchema,
  R.over(
    R.lensPath(['energiatodistus', 'lahtotiedot']),
    R.pick(['lammitetty-nettoala'])
  ),
  R.over(
    R.lensPath(['energiatodistus', 'tulokset']),
    R.pick(['e-luku', 'e-luokka'])
  ),
  R.dissocPath(['energiatodistus', 'toteutunut-ostoenergiankulutus']),
  R.dissocPath(['energiatodistus', 'huomiot'])
)(schema);

export const paakayttajaSchema = R.compose(
  R.omit(['energiatodistus.laskuriviviite']),
  flattenSchema
)(schema);

export const flatSchema = flattenSchema(schema);
