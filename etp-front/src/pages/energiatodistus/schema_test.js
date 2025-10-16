// javascript
import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as schema from './schema';

describe('YearOnlyDate descriptor', () => {
  it('parse: accepts a year string and returns Either.Right(Maybe.of(year))', () => {
    expect(schema.YearOnlyDate().parse('2025')).toEqual(
      Either.Right(Maybe.of(2025))
    );
  });

  it('deserialize: parses Some("YYYY-MM-DD") into Right(Some(year)) using UTC-safe parsing', () => {
    const expected = Either.Right(Maybe.of(2025));
    const deserialized = schema
      .YearOnlyDate()
      .deserialize(Maybe.Some('2025-01-01'));
    expect(deserialized).toEqual(expected);
  });

  it('serialize: maps Either.Right(Maybe.of(year)) to Either.Right(Maybe.of("YYYY-01-01"))', () => {
    const input = Either.Right(Maybe.of(2025));
    const expected = Either.Right(Maybe.of('2025-01-01'));
    expect(schema.YearOnlyDate().serialize(input)).toEqual(expected);
  });

  it('serialize: preserves Maybe.None() inside Either', () => {
    const input = Either.Right(Maybe.None());
    expect(schema.YearOnlyDate().serialize(input)).toEqual(
      Either.Right(Maybe.None())
    );
  });

  it('validators: descriptor exposes an array of validators (empty expected)', () => {
    expect(Array.isArray(schema.YearOnlyDate().validators)).toBe(true);
    expect(schema.YearOnlyDate().validators.length).toBe(0);
  });
});
