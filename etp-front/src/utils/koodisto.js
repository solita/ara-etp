/**
 * @module Koodisto
 * @description Utilities for handling koodisto
 */

import * as R from 'ramda';
import * as Maybe from './maybe-utils';
import * as Either from './either-utils';

/**
 * @sig {string|number} -> Array a -> Maybe a
 */
export const findFromKoodistoById = R.curry((id, koodisto) =>
  R.compose(Maybe.fromNull, R.find(R.propEq('id', id)))(koodisto)
);

/**
 * @deprecated
 * @sig (a -> string) -> a -> string
 */
export const koodiLocale = R.curry((labelLocale, koodi) =>
  R.compose(
    Either.orSome(''),
    R.map(labelLocale),
    R.chain(Maybe.toEither(''))
  )(koodi)
);
