import { expect, describe, it } from '@jest/globals';
import * as Korvaus from './korvaus';
import * as Maybe from '@Utility/maybe-utils';
import * as ET from '../energiatodistus-utils';

/**
 * Unit tests for korvaus.js pure functions used in energiatodistus korvaavuus logic.
 *
 * These are regression tests verifying existing behavior. All should pass in the red phase.
 */
describe('korvaus.js', () => {
  describe('isSame', () => {
    it('given two energiatodistukset with the same id, when checking isSame, then returns true', () => {
      // given
      const a = { id: 1 };
      const b = { id: 1 };

      // when
      const result = Korvaus.isSame(a, b);

      // then
      expect(result).toBe(true);
    });

    it('given two energiatodistukset with different ids, when checking isSame, then returns false', () => {
      // given
      const a = { id: 1 };
      const b = { id: 2 };

      // when
      const result = Korvaus.isSame(a, b);

      // then
      expect(result).toBe(false);
    });
  });

  describe('isValidState', () => {
    it('given korvattava in signed state, when checking isValidState, then returns true', () => {
      // given
      const korvattava = { 'tila-id': ET.tila.signed };
      const energiatodistus = { 'tila-id': ET.tila.draft };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(true);
    });

    it('given korvattava in discarded state, when checking isValidState, then returns true', () => {
      // given
      const korvattava = { 'tila-id': ET.tila.discarded };
      const energiatodistus = { 'tila-id': ET.tila.draft };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(true);
    });

    it('given korvattava in draft state, when checking isValidState, then returns false', () => {
      // given
      const korvattava = { id: 10, 'tila-id': ET.tila.draft };
      const energiatodistus = {
        id: 20,
        'tila-id': ET.tila.draft,
        'korvattu-energiatodistus-id': Maybe.None()
      };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava in replaced state and energiatodistus is its korvaaja, when checking isValidState, then returns true', () => {
      // given
      const korvattava = {
        id: 10,
        'tila-id': ET.tila.replaced,
        'korvaava-energiatodistus-id': Maybe.Some(20)
      };
      const energiatodistus = {
        id: 20,
        'tila-id': ET.tila.signed,
        'korvattu-energiatodistus-id': Maybe.Some(10)
      };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(true);
    });

    it('given korvattava in replaced state but energiatodistus does not point to it, when checking isValidState, then returns false', () => {
      // given
      const korvattava = {
        id: 10,
        'tila-id': ET.tila.replaced
      };
      const energiatodistus = {
        id: 20,
        'tila-id': ET.tila.signed,
        'korvattu-energiatodistus-id': Maybe.Some(999)
      };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava in expired state, when checking isValidState, then returns false', () => {
      // given
      const korvattava = { id: 10, 'tila-id': ET.tila.expired };
      const energiatodistus = {
        id: 20,
        'tila-id': ET.tila.signed,
        'korvattu-energiatodistus-id': Maybe.None()
      };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava in deleted state, when checking isValidState, then returns false', () => {
      // given
      const korvattava = { id: 10, 'tila-id': ET.tila.deleted };
      const energiatodistus = {
        id: 20,
        'tila-id': ET.tila.signed,
        'korvattu-energiatodistus-id': Maybe.None()
      };

      // when
      const result = Korvaus.isValidState(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });
  });

  describe('hasOtherKorvaaja', () => {
    it('given korvattava with no korvaaja, when checking hasOtherKorvaaja, then returns false', () => {
      // given
      const korvattava = { 'korvaava-energiatodistus-id': Maybe.None() };
      const energiatodistus = { id: 1 };

      // when
      const result = Korvaus.hasOtherKorvaaja(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava where same ET is korvaaja, when checking hasOtherKorvaaja, then returns false', () => {
      // given
      const korvattava = { 'korvaava-energiatodistus-id': Maybe.Some(1) };
      const energiatodistus = { id: 1 };

      // when
      const result = Korvaus.hasOtherKorvaaja(korvattava, energiatodistus);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava where different ET is korvaaja, when checking hasOtherKorvaaja, then returns true', () => {
      // given
      const korvattava = { 'korvaava-energiatodistus-id': Maybe.Some(2) };
      const energiatodistus = { id: 1 };

      // when
      const result = Korvaus.hasOtherKorvaaja(korvattava, energiatodistus);

      // then
      expect(result).toBe(true);
    });
  });

  describe('isValidLocation', () => {
    it('given matching postinumero and kayttotarkoitus, when checking isValidLocation, then returns true', () => {
      // given
      const a = {
        perustiedot: { postinumero: '33100', kayttotarkoitus: 'YAT' }
      };
      const b = {
        perustiedot: { postinumero: '33100', kayttotarkoitus: 'YAT' }
      };

      // when
      const result = Korvaus.isValidLocation(a, b);

      // then
      expect(result).toBe(true);
    });

    it('given different postinumero, when checking isValidLocation, then returns false', () => {
      // given
      const a = {
        perustiedot: { postinumero: '33100', kayttotarkoitus: 'YAT' }
      };
      const b = {
        perustiedot: { postinumero: '00100', kayttotarkoitus: 'YAT' }
      };

      // when
      const result = Korvaus.isValidLocation(a, b);

      // then
      expect(result).toBe(false);
    });

    it('given different kayttotarkoitus, when checking isValidLocation, then returns false', () => {
      // given
      const a = {
        perustiedot: { postinumero: '33100', kayttotarkoitus: 'YAT' }
      };
      const b = { perustiedot: { postinumero: '33100', kayttotarkoitus: 'T' } };

      // when
      const result = Korvaus.isValidLocation(a, b);

      // then
      expect(result).toBe(false);
    });
  });

  // ---- AE-2759: Yksinkertaistettu päivitysmenettely tests ----

  describe('isReplacedCertificateValid', () => {
    it('given korvattava whose voimassaolo-paattymisaika is in the future, when checking validity, then returns true', () => {
      // given
      const futureDate = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString();
      const korvattava = {
        'voimassaolo-paattymisaika': Maybe.Some(futureDate)
      };

      // when
      const result = Korvaus.isReplacedCertificateValid(korvattava);

      // then
      expect(result).toBe(true);
    });

    it('given korvattava whose voimassaolo-paattymisaika is in the past, when checking validity, then returns false', () => {
      // given
      const pastDate = new Date(Date.now() - 365 * 24 * 60 * 60 * 1000).toISOString();
      const korvattava = {
        'voimassaolo-paattymisaika': Maybe.Some(pastDate)
      };

      // when
      const result = Korvaus.isReplacedCertificateValid(korvattava);

      // then
      expect(result).toBe(false);
    });

    it('given korvattava whose voimassaolo-paattymisaika is None, when checking validity, then returns false', () => {
      // given
      const korvattava = {
        'voimassaolo-paattymisaika': Maybe.None()
      };

      // when
      const result = Korvaus.isReplacedCertificateValid(korvattava);

      // then
      expect(result).toBe(false);
    });
  });

  describe('canUseSimplifiedProcedure', () => {
    it('given draft ET with valid replacement target, when checking canUseSimplifiedProcedure, then returns true', () => {
      // given
      const futureDate = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString();
      const energiatodistus = {
        'tila-id': ET.tila.draft,
        'korvattu-energiatodistus-id': Maybe.Some(10)
      };
      const korvattava = {
        id: 10,
        'tila-id': ET.tila.signed,
        'voimassaolo-paattymisaika': Maybe.Some(futureDate)
      };

      // when
      const result = Korvaus.canUseSimplifiedProcedure(energiatodistus, korvattava);

      // then
      expect(result).toBe(true);
    });

    it('given draft ET with no replacement target, when checking canUseSimplifiedProcedure, then returns false', () => {
      // given
      const energiatodistus = {
        'tila-id': ET.tila.draft,
        'korvattu-energiatodistus-id': Maybe.None()
      };
      const korvattava = null;

      // when
      const result = Korvaus.canUseSimplifiedProcedure(energiatodistus, korvattava);

      // then
      expect(result).toBe(false);
    });

    it('given signed ET with valid replacement target, when checking canUseSimplifiedProcedure, then returns false', () => {
      // given
      const futureDate = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString();
      const energiatodistus = {
        'tila-id': ET.tila.signed,
        'korvattu-energiatodistus-id': Maybe.Some(10)
      };
      const korvattava = {
        id: 10,
        'tila-id': ET.tila.signed,
        'voimassaolo-paattymisaika': Maybe.Some(futureDate)
      };

      // when
      const result = Korvaus.canUseSimplifiedProcedure(energiatodistus, korvattava);

      // then
      expect(result).toBe(false);
    });

    it('given draft ET with expired replacement target, when checking canUseSimplifiedProcedure, then returns false', () => {
      // given
      const pastDate = new Date(Date.now() - 365 * 24 * 60 * 60 * 1000).toISOString();
      const energiatodistus = {
        'tila-id': ET.tila.draft,
        'korvattu-energiatodistus-id': Maybe.Some(10)
      };
      const korvattava = {
        id: 10,
        'tila-id': ET.tila.signed,
        'voimassaolo-paattymisaika': Maybe.Some(pastDate)
      };

      // when
      const result = Korvaus.canUseSimplifiedProcedure(energiatodistus, korvattava);

      // then
      expect(result).toBe(false);
    });
  });
});
