import * as R from 'ramda';
import moment from 'moment';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

import * as Kayttaja from '@Component/Kayttaja/kayttaja-utils';

import * as Validation from '@Utility/validation';

export const laatijaApi = `api/private/laatijat`;

export const urlForLaatijaId = id => `${laatijaApi}/${id}`;

export const formSchema = () => ({
  henkilotunnus: [Validation.isRequired, Validation.henkilotunnusValidator],
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
  postinumero: [Validation.isRequired, Validation.postinumeroValidator],
  postitoimipaikka: [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)
  ]
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
  maa: Either.Right,
  toimintaalue: Maybe.fromNull
});

export const serialize = R.compose(
  R.evolve({ maa: Either.right, toimintaalue: Maybe.orSome(null) }),
  R.omit(['id', 'email', 'login'])
);

export const serializeForLaatija = R.compose(
  R.omit([
    'passivoitu',
    'patevyystaso',
    'toteamispaivamaara',
    'toteaja',
    'laatimiskielto'
  ]),
  serialize
);

export const serializeImport = R.evolve({
  toteamispaivamaara: R.curry(date => moment(date).format('YYYY-MM-DD'))
});

export const laatijaFromKayttaja = R.compose(
  R.converge(R.merge, [
    R.pick([
      'email',
      'etunimi',
      'sukunimi',
      'puhelin',
      'passivoitu',
      'rooli',
      'login'
    ]),
    R.compose(R.dissoc('kayttaja'), R.prop('laatija'))
  ])
);

export const emptyLaatija = () =>
  deserialize({
    jakeluosoite: '',
    muuttoimintaalueet: [],
    toteamispaivamaara: '1970-01-01',
    puhelin: '',
    sukunimi: '',
    maa: '',
    patevyystaso: 0,
    postitoimipaikka: '',
    postinumero: '',
    ensitallennus: true,
    julkinenemail: false,
    email: '',
    patevyysvoimassa: false,
    henkilotunnus: '',
    toimintaalue: Maybe.None(),
    etunimi: '',
    julkinenpuhelin: false,
    login: Maybe.None()
  });

export const getLaatijaByIdFuture = R.curry((fetch, id) =>
  R.compose(
    R.map(R.compose(deserialize, laatijaFromKayttaja)),
    Kayttaja.kayttajaAndLaatijaFuture(fetch)
  )(id)
);

export const putLaatijaByIdFuture = R.curry((rooli, fetch, id, laatija) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Kayttaja.putKayttajanLaatijaFuture(fetch, id),
    R.ifElse(
      R.equals(2),
      R.always(serialize),
      R.always(serializeForLaatija)
    )(rooli)
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
  toteamispaivamaara: Validation.isPaivamaara,
  maa: Validation.isFilled
};

// Maa defaults to Finland as the transfer file does not have it as a field
export const readRow = R.ifElse(
  R.compose(R.equals(R.length(dataFields)), R.length, R.split(';')),
  R.compose(
    R.evolve(parse),
    R.assoc('maa', 'FI'),
    R.zipObj(dataFields),
    R.split(';')
  ),
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
