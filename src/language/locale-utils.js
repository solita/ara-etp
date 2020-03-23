import * as R from 'ramda';

export const label = R.curry((locale, item) =>
  R.prop(`label-${R.compose(R.head, R.split('-'))(locale)}`, item)
);
