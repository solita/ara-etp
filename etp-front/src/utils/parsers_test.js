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
