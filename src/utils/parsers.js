/**
 * @module Parsers
 * @description Parsers for different types
 *
 * A parser converts string to some other value type string -> a e.g. string -> number.
 *
 * If parsing may fail i.e. there are string that do not represent any value in target type a,
 * then the failure or success maybe represented using Either [(Translate -> string), a].
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
  R.map(Either.map(Maybe.Some))
);

/**
 * @sig string -> Maybe string
 */
export const optionalString = R.compose(Maybe.fromEmpty, R.trim);

/**
 * Convert a parser function to a version where input string maybe empty or nil.
 * The parsed value is wrapped in Either and Maybe (respectively).
 * The result is interpreted as
 * - Either.Left(errorFn) means that the parsing has failed
 * - Either.Right(Maybe.None()) means that the input string was empty or nil. The original parser was never used.
 * - Either.Right(Maybe.Some(value)) means that the value was successfully parsed
 *
 * @sig (a -> Either[(Translate -> string),a]) -> Either [(Translate -> string),Maybe a]
 */
export const optionalParser = parse =>
  R.compose(toEitherMaybe, R.map(parse), optionalString);

/**
 * Convert a parser function to a version where we only care about the value.
 * Parse failure or missing value are all represented as Maybe.None().
 * The successfully parsed value is wrapped in Maybe[a].
 *
 * @sig (string -> Either[(Translate -> string),a]) -> (string -> Maybe[a])
 */
export const toMaybe = parse =>
  R.compose(R.chain(Either.toMaybe), R.map(parse), optionalString);
