import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

export const selectedItem = R.curry((filteredItems, active) =>
  R.compose(Maybe.fromNull, R.nth(R.__, filteredItems))(active)
);

export const previousItem = R.compose(R.filter(R.lte(0)), Maybe.Some, R.dec);

export const nextItem = R.curry((filteredItems, active) =>
  R.compose(Maybe.Some, R.min(R.inc(active)), R.dec, R.length)(filteredItems)
);
