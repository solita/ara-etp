import * as R from 'ramda';
import * as Maybe from './maybe-utils';
import * as Either from './either-utils';

export const findFromKoodistoById = R.curry((id, koodisto) =>
  R.compose(Maybe.fromNull, R.find(R.propEq('id', id)))(koodisto)
);

export const koodiLocale = R.curry((labelLocale, koodi) =>
  R.compose(
    Either.orSome(''),
    R.map(labelLocale),
    R.chain(Maybe.toEither(''))
  )(koodi)
);
