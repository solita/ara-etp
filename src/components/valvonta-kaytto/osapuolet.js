import * as R from 'ramda';

export const isOmistaja = R.compose(R.equals(0), R.prop('rooli-id'));
