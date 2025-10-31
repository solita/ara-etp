import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';
import * as formats from '@Utility/formats';
import * as fxmath from '@Utility/fxmath';

export const formatCost = R.compose(
  Maybe.orSome('-'),
  R.map(R.compose(formats.numberFormat, fxmath.round(2))),
  R.lift(R.divide(R.__, 100))
);

export const formatCostDifference = R.compose(
  Maybe.orSome('-'),
  R.map(
    R.ifElse(
      a => a > 0,
      R.compose(s => '+' + formats.numberFormat(s), fxmath.round(2)),
      R.compose(formats.numberFormat, fxmath.round(2))
    )
  ),
  R.lift(R.divide(R.__, 100))
);
