/**
 * @module Parsers
 * @description Parsers for different types
 */
import * as R from 'ramda';
import * as dfns from 'date-fns';

import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as Validation from '@Utility/validation';

/**
 * @description Parses integers from string and returns results wrapped in Either
 *
 * @sig string -> Either [(Translate -> string),number]
 */
export const parseInteger = R.compose(
  R.ifElse(
    R.test(/^\d+$/),
    R.compose(Either.Right, txt => parseInt(txt)),
    R.always(Either.Left(R.applyTo('parsing.invalid-integer')))
  ),
  R.replace(/\s/g, '')
);

/**
 * @sig string -> Either [(Translate -> string),number]
 */
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

/**
 * @sig string -> Either [(Translate -> string),number]
 */
export const parsePercent = R.compose(
  R.map(R.divide(R.__, 100)),
  parseNumber,
  R.trim,
  R.replace('%', '')
);

/**
 * @sig string -> Either [(Translate -> string),number]
 */
export const parseDayCount = R.compose(
  parseNumber,
  /* Trim out non-digits */
  R.replace(/\D/g, '')
);

/**
 * @sig string -> Either [(Translate -> string),Date]
 */
export const parseDate = R.compose(
  R.ifElse(
    dfns.isValid,
    Either.Right,
    R.always(Either.Left(R.applyTo('parsing.invalid-date')))
  ),
  date => dfns.parse(date, Validation.DATE_FORMAT, 0),
  R.trim
);

/**
 * @sig string -> Either [(Translate -> string),Date]
 */
export const parseISODate = R.compose(
  R.ifElse(
    dfns.isValid,
    Either.Right,
    R.always(Either.Left(R.applyTo('parsing.invalid-date')))
  ),
  dfns.parseISO
);

/**
 * @sig string -> string
 */
export const addDefaultProtocol = R.ifElse(
  R.anyPass([R.includes('://'), R.isEmpty]),
  R.identity,
  R.concat('http://')
);

/**
 * @sig Maybe [Either[*,a]] -> Either [*,Maybe a]
 */
export const toEitherMaybe = R.compose(
  Maybe.orSome(Either.Right(Maybe.None())),
  Maybe.map(Either.map(Maybe.Some))
);

/**
 * @sig string -> Maybe string
 */
export const optionalString = R.compose(Maybe.fromEmpty, R.trim);

/**
 * @sig (a -> Either[(Translate -> string),a]) -> Either [*,Maybe a]
 */
export const optionalParser = parse =>
  R.compose(toEitherMaybe, Maybe.map(parse), optionalString);
