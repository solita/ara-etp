import * as R from 'ramda';
import * as dfns from 'date-fns';

import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

import * as Kayttaja from '@Component/Kayttaja/kayttaja-utils';

import * as Validation from '@Utility/validation';

export const laatijaApi = `api/private/laatijat`;

export const deserialize = R.compose(
  R.assoc('api-key', Maybe.None()),
  R.evolve({
    'vastaanottajan-tarkenne': Maybe.fromNull,
    henkilotunnus: Maybe.fromNull,
    maa: Either.Right,
    toimintaalue: Maybe.fromNull,
    wwwosoite: Maybe.fromNull
  })
);

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
      'login',
      'henkilotunnus'
    ]),
    R.compose(R.dissoc('kayttaja'), R.prop('laatija'))
  ])
);

export const putLaatijatFuture = R.curry((fetch, laatijat) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', laatijaApi)),
    R.map(serializeImport)
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
  henkilotunnus: R.compose(R.toUpper, R.trim),
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

export const formatVoimassaoloaika = toteamispaivamaara =>
  `${dfns.format(
    dfns.parse(toteamispaivamaara, 'yyyy-M-d', 0),
    'd.M.yyyy'
  )} - ${dfns.format(
    dfns.add(dfns.parse(toteamispaivamaara, 'yyyy-M-d', 0), { years: 7 }),
    'd.M.yyyy'
  )}`;
