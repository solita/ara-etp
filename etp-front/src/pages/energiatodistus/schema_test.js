// javascript
import { expect, describe, it } from '@jest/globals';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as schema from './schema';

describe('PPPVaiheYear descriptor', () => {
  it('parse: accepts a year string and returns Either.Right(Maybe.of(year))', () => {
    expect(schema.PPPVaiheYear().parse('2025')).toEqual(
      Either.Right(Maybe.of(2025))
    );
  });

  it('deserialize: parses Some("YYYY-MM-DD") into Right(Some(year)) using UTC-safe parsing', () => {
    const expected = Either.Right(Maybe.of(2025));
    const deserialized = schema
      .PPPVaiheYear()
      .deserialize(Maybe.Some('2025-01-01'));
    expect(deserialized).toEqual(expected);
  });

  it('serialize: maps Either.Right(Maybe.of(year)) to Either.Right(Maybe.of("YYYY-01-01"))', () => {
    const input = Either.Right(Maybe.of(2025));
    const expected = Either.Right(Maybe.of('2025-01-01'));
    expect(schema.PPPVaiheYear().serialize(input)).toEqual(expected);
  });

  it('serialize: preserves Maybe.None() inside Either', () => {
    const input = Either.Right(Maybe.None());
    expect(schema.PPPVaiheYear().serialize(input)).toEqual(
      Either.Right(Maybe.None())
    );
  });

  it('validators: descriptor exposes an array of validators (at least one expected)', () => {
    expect(Array.isArray(schema.PPPVaiheYear().validators)).toBe(true);
    expect(schema.PPPVaiheYear().validators.length).toBeGreaterThan(0);
  });
});
