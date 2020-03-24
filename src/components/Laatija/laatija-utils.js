import * as R from 'ramda';
import moment from 'moment';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Either from '@Utility/either-utils';

import * as Validation from '@Utility/validation';

export const laatijaApi = `api/private/laatijat`;

export const urlForLaatijaId = id => `${laatijaApi}/${id}`;

export const formSchema = () => ({
  henkilotunnus: [],
  etunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  sukunimi: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  email: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  puhelin: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  jakeluosoite: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ],
  postinumero: [Validation.isRequired, Validation.isPostinumero],
  postitoimipaikka: []
});

export const formParsers = () => ({
  henkilotunnus: R.trim,
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim
});

export const deserialize = R.evolve({
  maa: Either.Right
});

export const serialize = R.compose(
  R.evolve({ maa: Either.right }),
  R.dissoc('id')
);

export const serializeImport = R.evolve({
  toteamispaivamaara: R.curry(date => moment(date).format('YYYY-MM-DD'))
});

export const emptyLaatija = () =>
  deserialize({
    jakeluosoite: '',
    'muut-toimintaalueet': [0],
    'patevyys-voimassaoloaika': {
      start: '2020-03-17',
      end: '2020-03-17'
    },
    puhelin: '',
    sukunimi: '',
    maa: '',
    patevyys: 0,
    postitoimipaikka: '',
    postinumero: '',
    ensitallennus: true,
    'julkinen-email': true,
    email: '',
    'patevyys-voimassa': true,
    henkilotunnus: '',
    toimintaalue: 0,
    etunimi: '',
    'julkinen-puhelin': true
  });

export const getyLaatijaByIdFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(deserialize),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    urlForLaatijaId
  )(id)
);

export const putLaatijaByIdFuture = R.curry((fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', urlForLaatijaId(id))),
    serialize
  )(laatija)
);

export const putLaatijatFuture = R.curry((fetch, laatijat) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', laatijaApi)),
    R.map(serializeImport)
  )(laatijat)
);

export const postLaatijatFuture = R.curry((fetch, laatijat) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', laatijaApi)),
    R.map(serialize)
  )(laatijat)
);

export const dataFields = [
  'toteaja',
  'etunimi',
  'sukunimi',
  'henkilotunnus',
  'jakeluosoite',
  'postinumero',
  'postitoimipaikka',
  'email',
  'puhelin',
  'patevyystaso',
  'toteamispaivamaara'
];

export const parse = {
  toteaja: R.trim,
  etunimi: R.trim,
  sukunimi: R.trim,
  henkilotunnus: R.trim,
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  email: R.trim,
  puhelin: R.trim,
  patevyystaso: R.compose(parseInt, R.trim),
  toteamispaivamaara: R.compose(
    R.curry(date => {
      const momentDate = moment(date, Validation.DATE_FORMAT);
      return momentDate.isValid() ? momentDate.toDate() : date;
    }),
    R.trim
  )
};

export const validate = {
  toteaja: Validation.isLaatijanToteaja,
  etunimi: Validation.isFilled,
  sukunimi: Validation.isFilled,
  henkilotunnus: Validation.isValidHenkilotunnus,
  jakeluosoite: Validation.isFilled,
  postinumero: Validation.isPostinumero,
  postitoimipaikka: Validation.isFilled,
  email: Validation.isValidEmail,
  puhelin: Validation.isPuhelin,
  patevyystaso: Validation.isPatevyystaso,
  toteamispaivamaara: Validation.isPaivamaara
};

// Maa defaults to Finland as the transfer file does not have it as a field
export const readRow = R.ifElse(
  R.compose(R.equals(R.length(dataFields)), R.length, R.split(';')),
  R.compose(R.evolve(parse), R.assoc('maa', 'FI'), R.zipObj(dataFields), R.split(';')),
  R.always(null)
);

export const readData = R.ifElse(
  R.compose(R.equals('String'), R.type),
  R.compose(
    R.filter(R.complement(R.isNil)),
    R.map(readRow),
    R.split(/\r?\n/g),
    R.trim
  ),
  R.always([])
);

export const rowValid = R.compose(
  R.reduce(R.and, true),
  R.values,
  R.evolve(validate)
);

export const dataValid = R.ifElse(
  R.allPass([R.complement(R.isEmpty), R.compose(R.equals('Array'), R.type)]),
  R.compose(R.reduce(R.and, true), R.map(rowValid)),
  R.always(false)
);
