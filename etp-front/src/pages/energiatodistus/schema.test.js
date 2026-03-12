// javascript
import { expect, describe, it } from '@jest/globals';
import * as Either from '@Utility/either-utils';
import * as Maybe from '@Utility/maybe-utils';
import * as schema from './schema';

// --- Part 1: Schema Numeric Type Fix ---

describe('Ilmastoselvitys schema numeric types', () => {
  // Test 1.1: Hiilijalanjälki rakennus fields accept decimal values
  describe('hiilijalanjalki rakennus fields accept decimal values', () => {
    const fields = [
      'rakennustuotteiden-valmistus',
      'kuljetukset-tyomaavaihe',
      'rakennustuotteiden-vaihdot',
      'energiankaytto',
      'purkuvaihe'
    ];

    it.each(fields)(
      'given a decimal string, when parsing %s, then returns Maybe.Some with the decimal value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilijalanjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('12.5');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(12.5)));
      }
    );

    it.each(fields)(
      'given a negative decimal string, when parsing %s, then returns Maybe.Some with the negative value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilijalanjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('-3.7');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(-3.7)));
      }
    );

    it.each(fields)(
      'given an integer string, when parsing %s, then returns Maybe.Some with the integer value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilijalanjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('100');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(100)));
      }
    );
  });

  // Test 1.2: Hiilijalanjälki rakennuspaikka fields accept decimal values
  describe('hiilijalanjalki rakennuspaikka fields accept decimal values', () => {
    const fields = [
      'rakennustuotteiden-valmistus',
      'kuljetukset-tyomaavaihe',
      'rakennustuotteiden-vaihdot',
      'energiankaytto',
      'purkuvaihe'
    ];

    it.each(fields)(
      'given a decimal string, when parsing %s, then returns Maybe.Some with the decimal value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilijalanjalki.rakennuspaikka[field];

        // when
        const result = fieldSchema.parse('12.5');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(12.5)));
      }
    );

    it.each(fields)(
      'given a negative decimal string, when parsing %s, then returns Maybe.Some with the negative value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilijalanjalki.rakennuspaikka[field];

        // when
        const result = fieldSchema.parse('-3.7');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(-3.7)));
      }
    );
  });

  // Test 1.3: Hiilikädenjälki rakennus fields accept decimal values
  describe('hiilikadenjalki rakennus fields accept decimal values', () => {
    const fields = [
      'uudelleenkaytto',
      'kierratys',
      'ylimaarainen-uusiutuvaenergia',
      'hiilivarastovaikutus',
      'karbonatisoituminen'
    ];

    it.each(fields)(
      'given a decimal string, when parsing %s, then returns Maybe.Some with the decimal value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilikadenjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('12.5');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(12.5)));
      }
    );

    it.each(fields)(
      'given a negative decimal string, when parsing %s, then returns Maybe.Some with the negative value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilikadenjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('-3.7');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(-3.7)));
      }
    );

    it.each(fields)(
      'given an integer string, when parsing %s, then returns Maybe.Some with the integer value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilikadenjalki.rakennus[field];

        // when
        const result = fieldSchema.parse('100');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(100)));
      }
    );
  });

  // Test 1.4: Hiilikädenjälki rakennuspaikka fields accept decimal values
  describe('hiilikadenjalki rakennuspaikka fields accept decimal values', () => {
    const fields = [
      'uudelleenkaytto',
      'kierratys',
      'ylimaarainen-uusiutuvaenergia',
      'hiilivarastovaikutus',
      'karbonatisoituminen'
    ];

    it.each(fields)(
      'given a decimal string, when parsing %s, then returns Maybe.Some with the decimal value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilikadenjalki.rakennuspaikka[field];

        // when
        const result = fieldSchema.parse('12.5');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(12.5)));
      }
    );

    it.each(fields)(
      'given a negative decimal string, when parsing %s, then returns Maybe.Some with the negative value',
      field => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys.hiilikadenjalki.rakennuspaikka[field];

        // when
        const result = fieldSchema.parse('-3.7');

        // then
        expect(result).toEqual(Either.Right(Maybe.Some(-3.7)));
      }
    );
  });

  // Test 1.5: Ilmastoselvitys numeric fields use AnyFloat range (negative values allowed)
  describe('ilmastoselvitys numeric fields accept negative values (AnyFloat range)', () => {
    const hiilijalanjalkiFields = [
      ['hiilijalanjalki', 'rakennus', 'rakennustuotteiden-valmistus'],
      ['hiilijalanjalki', 'rakennus', 'kuljetukset-tyomaavaihe'],
      ['hiilijalanjalki', 'rakennus', 'rakennustuotteiden-vaihdot'],
      ['hiilijalanjalki', 'rakennus', 'energiankaytto'],
      ['hiilijalanjalki', 'rakennus', 'purkuvaihe'],
      ['hiilijalanjalki', 'rakennuspaikka', 'rakennustuotteiden-valmistus'],
      ['hiilijalanjalki', 'rakennuspaikka', 'kuljetukset-tyomaavaihe'],
      ['hiilijalanjalki', 'rakennuspaikka', 'rakennustuotteiden-vaihdot'],
      ['hiilijalanjalki', 'rakennuspaikka', 'energiankaytto'],
      ['hiilijalanjalki', 'rakennuspaikka', 'purkuvaihe']
    ];

    const hiilikadenjalkiFields = [
      ['hiilikadenjalki', 'rakennus', 'uudelleenkaytto'],
      ['hiilikadenjalki', 'rakennus', 'kierratys'],
      ['hiilikadenjalki', 'rakennus', 'ylimaarainen-uusiutuvaenergia'],
      ['hiilikadenjalki', 'rakennus', 'hiilivarastovaikutus'],
      ['hiilikadenjalki', 'rakennus', 'karbonatisoituminen'],
      ['hiilikadenjalki', 'rakennuspaikka', 'uudelleenkaytto'],
      ['hiilikadenjalki', 'rakennuspaikka', 'kierratys'],
      ['hiilikadenjalki', 'rakennuspaikka', 'ylimaarainen-uusiutuvaenergia'],
      ['hiilikadenjalki', 'rakennuspaikka', 'hiilivarastovaikutus'],
      ['hiilikadenjalki', 'rakennuspaikka', 'karbonatisoituminen']
    ];

    const allFields = [...hiilijalanjalkiFields, ...hiilikadenjalkiFields];

    it.each(allFields)(
      'given the field at %s.%s.%s, when validating negative float, then all validators pass',
      (section, subsection, field) => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys[section][subsection][field];
        const value = Maybe.Some(-100.5);

        // when / then
        fieldSchema.validators.forEach(validator => {
          expect(validator.predicate(value)).toBe(true);
        });
      }
    );

    it.each(allFields)(
      'given the field at %s.%s.%s, when validating zero, then all validators pass',
      (section, subsection, field) => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys[section][subsection][field];
        const value = Maybe.Some(0);

        // when / then
        fieldSchema.validators.forEach(validator => {
          expect(validator.predicate(value)).toBe(true);
        });
      }
    );

    it.each(allFields)(
      'given the field at %s.%s.%s, when validating positive float, then all validators pass',
      (section, subsection, field) => {
        // given
        const fieldSchema =
          schema.v2026.ilmastoselvitys[section][subsection][field];
        const value = Maybe.Some(999.99);

        // when / then
        fieldSchema.validators.forEach(validator => {
          expect(validator.predicate(value)).toBe(true);
        });
      }
    );
  });

  // Test 1.6: String fields remain unchanged
  describe('ilmastoselvitys string fields remain unchanged', () => {
    it('given the laatija field, when parsing a string, then returns a Maybe string value', () => {
      // given
      const laatija = schema.v2026.ilmastoselvitys.laatija;

      // when
      const result = laatija.parse('Test Laatija');

      // then
      expect(result).toEqual(Maybe.Some('Test Laatija'));
    });

    it('given the laatimisajankohta field, then it has a parse function (DateValue)', () => {
      // given
      const laatimisajankohta = schema.v2026.ilmastoselvitys.laatimisajankohta;

      // then
      expect(typeof laatimisajankohta.parse).toBe('function');
      expect(typeof laatimisajankohta.format).toBe('function');
    });
  });
});

// --- Part 5: Non-Regression — Version Isolation ---

describe('Version isolation for ilmastoselvitys', () => {
  // Test 5.1: 2018 schema does not contain ilmastoselvitys
  it('given the 2018 schema, then it does not have an ilmastoselvitys key', () => {
    // given / when
    const has = 'ilmastoselvitys' in schema.v2018;

    // then
    expect(has).toBe(false);
  });

  // Test 5.2: 2013 schema does not contain ilmastoselvitys
  it('given the 2013 schema, then it does not have an ilmastoselvitys key', () => {
    // given / when
    const has = 'ilmastoselvitys' in schema.v2013;

    // then
    expect(has).toBe(false);
  });
});
