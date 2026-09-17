import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as parsers from './parsers';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

describe('Parsers:', () => {
  describe('Parse integer:', () => {
    it('Success', () => {
      expect(parsers.parseInteger('1')).toEqual(Either.Right(1));
      expect(parsers.parseInteger('11')).toEqual(Either.Right(11));
      expect(parsers.parseInteger('111')).toEqual(Either.Right(111));
    });

    it('Parse optional integer', () => {
      const optionalParser = parsers.optionalParser(parsers.parseInteger);
      expect(optionalParser('1')).toEqual(Either.Right(Maybe.of(1)));
      expect(optionalParser('11')).toEqual(Either.Right(Maybe.of(11)));
      expect(optionalParser('111')).toEqual(Either.Right(Maybe.of(111)));

      expect(optionalParser('')).toEqual(Either.Right(Maybe.None()));
    });
  });

  describe('Parse date:', () => {
    it.each([
      ['12.12.00', 2000],
      ['12.12.12', 2012],
      ['12.12.99', 2099],
      ['12.12.99', 2099]
    ])('maps two-digit year in %s to the year %s', (input, expectedYear) => {
      const result = parsers.parseDate(input);

      expect(Either.isRight(result)).toBe(true);
      expect(Either.right(result).getFullYear()).toBe(expectedYear);
      expect(Either.right(result).getMonth()).toBe(11);
      expect(Either.right(result).getDate()).toBe(12);
    });

    it('keeps four-digit years unchanged', () => {
      const result = parsers.parseDate('12.12.1912');

      expect(Either.isRight(result)).toBe(true);
      expect(Either.right(result).getFullYear()).toBe(1912);
      expect(Either.right(result).getMonth()).toBe(11);
      expect(Either.right(result).getDate()).toBe(12);
    });

    it('two-digit years are inputtable when using leading zeros', () => {
      const result = parsers.parseDate('01.04.0001');

      expect(Either.isRight(result)).toBe(true);
      expect(Either.right(result).getFullYear()).toBe(1);
      expect(Either.right(result).getMonth()).toBe(3);
      expect(Either.right(result).getDate()).toBe(1);
    });
  });
});

describe('toEitherMaybe', () => {
  it('Converts Some Right to Right Some', () => {
    expect(parsers.toEitherMaybe(Maybe.Some(Either.Right(2025)))).toEqual(
      Either.Right(Maybe.of(2025))
    );
  });

  it('Converts None to Right None', () => {
    expect(parsers.toEitherMaybe(Maybe.None())).toEqual(
      Either.Right(Maybe.None())
    );
  });

  it('Converts Some Left to Left', () => {
    expect(parsers.toEitherMaybe(Maybe.Some(Either.Left('baaad')))).toEqual(
      Either.Left('baaad')
    );
  });
});
