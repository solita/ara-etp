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
});
