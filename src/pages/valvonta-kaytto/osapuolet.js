import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const isOmistaja = R.propEq('rooli-id', Maybe.Some(0));
export const otherRooli = R.propEq('toimitustapa-id', Maybe.Some(2));

export const otherToimitustapa = R.propEq('toimitustapa-id', Maybe.Some(2));
