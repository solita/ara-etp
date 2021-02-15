import * as R from 'ramda';
import { assert } from 'chai';
import * as parsers from './parsers';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';

describe('Parsers:', () => {
  describe('Parse integer:', () => {
    it('Success', () => {
      assert.deepEqual(parsers.parseInteger('1'), Either.Right(1));
      assert.deepEqual(parsers.parseInteger('11'), Either.Right(11));
      assert.deepEqual(parsers.parseInteger('111'), Either.Right(111));
    });

    it('Parse optional integer', () => {
      const optionalParser = parsers.optionalParser(parsers.parseInteger);
      assert.deepEqual(optionalParser('1'), Either.Right(Maybe.of(1)));
      assert.deepEqual(optionalParser('11'), Either.Right(Maybe.of(11)));
      assert.deepEqual(optionalParser('111'), Either.Right(Maybe.of(111)));

      assert.deepEqual(optionalParser(''), Either.Right(Maybe.None()));
    });
  });
});
