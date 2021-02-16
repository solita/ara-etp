import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Either from '@Utility/either-utils';
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

export const errorsForLaatija = R.curry((i18n, laatija) =>
  R.compose(
    R.join(' / '),
    R.map(Either.left),
    R.filter(Either.isLeft),
    R.values,
    R.mapObjIndexed((value, key, _) =>
      Either.leftMap(labelFn => `${key}: ${labelFn(i18n)}`, value)
    )
  )(laatija)
);

export const errors = R.curry((i18n, laatijat) =>
  R.compose(
    R.join(' | '),
    R.filter(R.length),
    R.addIndex(R.map)((laatija, index) =>
      R.compose(
        R.when(
          R.length,
          R.concat(`${i18n('errors.row-error')} ${index + 1}: `)
        ),
        errorsForLaatija(i18n)
      )(laatija)
    )
  )(laatijat)
);

export const deserialize = R.evolve({
  patevyystaso: parsers.parseInteger,
  toteamispaivamaara: parsers.parseDate
});

export const readRows = R.compose(
  R.map(
    R.compose(
      validate,
      deserialize,
      R.mergeLeft({ maa: 'FI' }),
      R.zipObj(dataFields),
      R.map(R.trim),
      R.split(';')
    )
  ),
  R.flatten,
  R.map(R.compose(R.split(/\r?\n/g)))
);

// Maa defaults to Finland as the transfer file does not have it as a field
// export const readRow = R.ifElse(
//   R.compose(R.equals(R.length(dataFields)), R.length, R.split(';')),
//   R.compose(
//     R.evolve(parse),
//     R.assoc('maa', 'FI'),
//     R.zipObj(dataFields),
//     R.split(';')
//   ),
//   R.always(null)
// );

// export const readData = R.ifElse(
//   R.compose(R.equals('String'), R.type),
//   R.compose(
//     R.filter(R.complement(R.isNil)),
//     R.map(readRow),
//     R.split(/\r?\n/g),
//     R.trim
//   ),
//   R.always([])
// );
