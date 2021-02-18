import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as parsers from '@Utility/parsers';
import * as Validation from '@Utility/validation';
import { schema } from '@Component/Laatija/schema';

export const futurizeFileRead = file =>
  Future.Future((reject, resolve) => {
    const reader = new FileReader();
    reader.onload = e => {
      resolve(e.target.result);
    };
    reader.onerror = e => {
      reject(e);
    };
    reader.readAsText(file, 'UTF-8');
    return () => {};
  });

const dataFields = [
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

const emptyUploadLaatija = {
  toteaja: '',
  etunimi: '',
  sukunimi: '',
  henkilotunnus: '',
  jakeluosoite: '',
  postinumero: '',
  postitoimipaikka: '',
  email: '',
  puhelin: '',
  patevyystaso: '',
  toteamispaivamaara: ''
};

const uploadSchema = {
  ...R.omit(
    ['henkilotunnus', 'vastaanottajan-tarkenne', 'wwwosoite', 'api-key'],
    schema
  ),
  henkilotunnus: [Validation.isRequired, Validation.henkilotunnusValidator],
  toteaja: [Validation.isRequired],
  patevyystaso: [Validation.isRequired],
  maa: [Validation.isRequired]
};

export const validate = Validation.validateModelObject(uploadSchema);

export const formatRowErrors = R.curry((i18n, rowErrors) =>
  R.compose(
    R.join(' / '),
    R.map(
      R.compose(
        R.join(' - '),
        R.over(R.lensIndex(0), R.compose(i18n, R.concat('laatijaupload.')))
      )
    ),
    R.toPairs
  )(rowErrors)
);

export const formatRowError = R.curry((i18n, index, rowError) =>
  R.compose(
    R.concat(`${i18n('errors.row-error')} ${index + 1}: `),
    formatRowErrors(i18n)
  )(rowError)
);

export const errorsForLaatija = R.curry((i18n, laatija) =>
  R.compose(
    R.reduce(R.mergeLeft, {}),
    R.map(Either.left),
    R.filter(Either.isLeft),
    R.values,
    R.mapObjIndexed((value, key, _) =>
      Either.leftMap(labelFn => ({ [key]: labelFn(i18n) }), value)
    )
  )(laatija)
);

export const errors = R.curry((i18n, laatijat) =>
  R.map(
    laatija =>
      R.compose(
        R.ifElse(R.compose(R.length, R.values), Maybe.Some, Maybe.None),
        errorsForLaatija(i18n)
      )(laatija),
    laatijat
  )
);

export const deserialize = R.evolve({
  patevyystaso: parsers.parseInteger,
  toteamispaivamaara: parsers.parseDate
});

export const readRows = R.compose(
  R.map(
    R.compose(
      R.mergeRight({ ...emptyUploadLaatija, maa: 'FI' }),
      R.zipObj(dataFields),
      R.map(R.trim),
      R.split(';')
    )
  ),
  R.flatten,
  R.map(R.compose(R.split(/\r?\n/g)))
);
