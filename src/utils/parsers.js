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
  R.replace(/\u2212/g, '-'),
  R.replace(/,/g, '.'),
  R.replace(/\s/g, '')
);

export const parsePercent = R.compose(
  R.map(R.divide(R.__, 100)),
  parseNumber,
  R.trim,
  R.replace('%', '')
);

export const parseDate = R.compose(
  R.ifElse(
    dfns.isValid,
    Either.Right,
    R.always(Either.Left(R.applyTo('parsing.invalid-date')))
  ),
  date => dfns.parse(date, Validation.DATE_FORMAT, 0),
  R.trim
);

export const parseISODate = R.compose(
  R.ifElse(
    dfns.isValid,
    Either.Right,
    R.always(Either.Left(R.applyTo('parsing.invalid-date')))
  ),
  dfns.parseISO
);

export const addDefaultProtocol = R.ifElse(
  R.anyPass([R.includes('://'), R.isEmpty]),
  R.identity,
  R.concat('http://')
);

/**
 * Transforms Maybe[Either[A]] -> Either[Maybe[A]]
 */
export const toEitherMaybe = R.compose(
  Maybe.orSome(Either.Right(Maybe.None())),
  Maybe.map(Either.map(Maybe.Some))
);

export const optionalString = R.compose(Maybe.fromEmpty, R.trim);

export const optionalParser = parse =>
  R.compose(toEitherMaybe, Maybe.map(parse), optionalString);
