import * as R from 'ramda';
import * as dfns from 'date-fns';

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
  'vastaanottajan-tarkenne': R.map(Validation.liftValidator, [
    Validation.isRequired,
    Validation.minLengthConstraint(2),
    Validation.maxLengthConstraint(200)]),
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
  ],
  wwwosoite: R.map(Validation.liftValidator, [Validation.urlValidator])
});

export const formParsers = () => ({
  henkilotunnus: R.trim,
  etunimi: R.trim,
  sukunimi: R.trim,
  email: R.trim,
  puhelin: R.trim,
  'vastaanottajan-tarkenne': R.compose(Maybe.fromEmpty, R.trim),
  jakeluosoite: R.trim,
  postinumero: R.trim,
  postitoimipaikka: R.trim,
  wwwosoite: R.compose(Maybe.fromEmpty, R.trim)
});

export const deserialize = R.evolve({
  'vastaanottajan-tarkenne': Maybe.fromNull,
  maa: Either.Right,
  toimintaalue: Maybe.fromNull,
  wwwosoite: Maybe.fromNull
});

export const serializeImport = R.evolve({
  toteamispaivamaara: date => dfns.format(date, 'yyyy-MM-dd')
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
    login: Maybe.None(),
    wwwosoite: Maybe.None(),
  });

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
  toteamispaivamaara: date =>
    R.compose(
      R.unless(dfns.isValid, R.always(date)),
      d => dfns.parse(d, Validation.DATE_FORMAT, 0),
      R.trim
    )(date)
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
  maa: Validation.isFilled,
  wwwosoite: Validation.isUrl
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
