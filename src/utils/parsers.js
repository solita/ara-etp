import * as R from 'ramda';
import * as dfns from 'date-fns';

import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

export const parseInteger = R.compose(
  R.ifElse(
    R.test(/^\d+$/),
    R.compose(Either.Right, txt => parseInt(txt)),
    R.always(Either.Left(R.applyTo('parsing.invalid-integer')))
  ),
  R.replace(/\s/g, '')
);

export const parseNumber = R.compose(
  R.ifElse(
    R.test(/^\-?\d+(\.\d+)?$/),
    R.compose(Either.Right, txt => parseFloat(txt)),
    R.always(Either.Left(R.applyTo('parsing.invalid-number')))
  ),
  R.replace(/,/g, '.'),
  R.replace(/\s/g, '')
);

export const parseDate = R.compose(
  R.ifElse(
    dfns.isValid,
    R.compose(Either.Right, date =>
      dfns.formatISO(date, { representation: 'date' })
    ),
    R.always(Either.Left(R.applyTo('parsing.invalid-date')))
  ),
  date => dfns.parse(date, Validation.DATE_FORMAT, 0),
  R.trim
);

export const optionalString = R.compose(Maybe.fromEmpty, R.trim);

export const optionalParser = parse =>
  R.compose(
    Maybe.orSome(Either.Right(Maybe.None())),
    Maybe.map(R.compose(Either.map(Maybe.Some), parse)),
    optionalString
  );
