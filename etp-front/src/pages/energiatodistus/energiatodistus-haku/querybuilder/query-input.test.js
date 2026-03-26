/**
 * @jest-environment jsdom
 */
import { expect, describe, it } from '@jest/globals';
import { OPERATOR_TYPES } from '../schema';

describe('query-input type mapping', () => {
  it('HAVAINNOINTIKAYNTITYYPPI type is defined for input mapping', () => {
    // Given: OPERATOR_TYPES from schema
    // When: checking HAVAINNOINTIKAYNTITYYPPI
    // Then: it is a defined string value
    expect(OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI).toBeDefined();
    expect(typeof OPERATOR_TYPES.HAVAINNOINTIKAYNTITYYPPI).toBe('string');
  });

  // REGRESSION: all existing types should still be defined after adding new ones
  it('all existing operator types are still defined (regression)', () => {
    // Given: the list of all pre-existing OPERATOR_TYPES keys
    const existingTypes = [
      'STRING',
      'NUMBER',
      'UNFORMATTED_NUMBER',
      'PERCENT',
      'DATE',
      'DATE_BETWEEN',
      'DAYCOUNT',
      'BOOLEAN',
      'VERSIO',
      'ELUOKKA',
      'VERSIOLUOKKA',
      'TILA',
      'VERSIOKAYTTOTARKOITUSLUOKKA',
      'LAATIJA',
      'LAATIMISVAIHE',
      'KIELISYYS',
      'ILMANVAIHTOTYYPPI',
      'PATEVYYSTASO',
      'KUNTA',
      'LAMMITYSMUOTO'
    ];

    // When: checking each type
    // Then: every existing type is still defined as a non-empty string
    existingTypes.forEach(type => {
      expect(OPERATOR_TYPES[type]).toBeDefined();
      expect(typeof OPERATOR_TYPES[type]).toBe('string');
    });
  });
});
