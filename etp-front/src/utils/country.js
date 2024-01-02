/**
 * @module Country
 * @description Utilities for handling country entities offered by backend
 */

import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

/**
 * @sig string -> Array Country -> Maybe Country
 * @description Find country by matching id
 */
export const findCountryById = R.curry((id, countries) =>
  Maybe.fromNull(R.find(R.propEq('id', id), countries))
);

/**
 * @sig string -> Array Country -> Maybe Country
 * @description Find country by matching id or either labels
 */
export const findCountry = R.curry((name, countries) =>
  R.compose(
    Maybe.fromNull,
    R.find(
      R.compose(
        R.includes(R.toLower(name)),
        R.map(R.toLower),
        R.props(['id', 'label-fi', 'label-sv'])
      )
    )
  )(countries)
);
