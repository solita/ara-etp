import * as R from 'ramda';

import * as Either from '@Utility/either-utils';
import * as Maybe from "@Utility/maybe-utils";

export const parseInteger = R.compose(
  R.ifElse(R.test(/^\d+$/),
    R.compose(Either.Right, txt => parseInt(txt)),
    R.always(Either.Left(R.applyTo('parsing.invalid-integer')))),
  R.replace(/\s/g, '')
);

export const parseNumber = R.compose(
  R.ifElse(R.test(/^\-?\d+(\.\d+)?$/),
    R.compose(Either.Right, txt => parseFloat(txt)),
    R.always(Either.Left(R.applyTo('parsing.invalid-number')))),
  R.replace(/,/g, '.'),
  R.replace(/\s/g, '')
);

export const optionalString = R.compose(Maybe.fromEmpty, R.trim);

export const optionalParser = parse => R.compose(
  Maybe.orSome(Either.Right(Maybe.None())),
  Maybe.map(R.compose(Either.map(Maybe.Some), parse)),
  optionalString
);