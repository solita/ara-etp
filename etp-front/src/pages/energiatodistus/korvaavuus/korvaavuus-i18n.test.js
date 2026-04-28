import { expect, describe, it } from '@jest/globals';
import fi from '@Language/fi.json';
import sv from '@Language/sv.json';

/**
 * Tests that korvaavuus-related i18n keys exist for the PPP column in the
 * korvaavuus energiatodistus table.
 *
 * AE-2620: PPP number should be displayed for korvattava energiatodistus.
 */
describe('Korvaavuus i18n labels', () => {
  describe('PPP table column header', () => {
    it('given fi.json, when checking for PPP column key in korvaavuus table, then key exists', () => {
      // given
      const table = fi.energiatodistus.korvaavuus.table;

      // then
      expect(table.ppp).toBeDefined();
      expect(table.ppp.length).toBeGreaterThan(0);
    });

    it('given sv.json, when checking for PPP column key in korvaavuus table, then key exists', () => {
      // given
      const table = sv.energiatodistus.korvaavuus.table;

      // then
      expect(table.ppp).toBeDefined();
      expect(table.ppp.length).toBeGreaterThan(0);
    });
  });

  // ---- AE-2759: Yksinkertaistettu päivitysmenettely i18n ----

  describe('Yksinkertaistettu päivitysmenettely labels', () => {
    it('given fi.json, when checking for yksinkertaistettu label, then key exists', () => {
      // given
      const korvaavuus = fi.energiatodistus.korvaavuus;

      // then
      expect(korvaavuus['yksinkertaistettu-paivitysmenettely']).toBeDefined();
      expect(
        korvaavuus['yksinkertaistettu-paivitysmenettely'].length
      ).toBeGreaterThan(0);
    });

    it('given sv.json, when checking for yksinkertaistettu label, then key exists', () => {
      // given
      const korvaavuus = sv.energiatodistus.korvaavuus;

      // then
      expect(korvaavuus['yksinkertaistettu-paivitysmenettely']).toBeDefined();
      expect(
        korvaavuus['yksinkertaistettu-paivitysmenettely'].length
      ).toBeGreaterThan(0);
    });

    it('given fi.json, when checking for yksinkertaistettu info text, then key exists', () => {
      // given
      const korvaavuus = fi.energiatodistus.korvaavuus;

      // then
      expect(korvaavuus['yksinkertaistettu-info']).toBeDefined();
      expect(korvaavuus['yksinkertaistettu-info'].length).toBeGreaterThan(0);
    });

    it('given sv.json, when checking for yksinkertaistettu info text, then key exists', () => {
      // given
      const korvaavuus = sv.energiatodistus.korvaavuus;

      // then
      expect(korvaavuus['yksinkertaistettu-info']).toBeDefined();
      expect(korvaavuus['yksinkertaistettu-info'].length).toBeGreaterThan(0);
    });
  });
});
