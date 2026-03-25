import { expect, describe, it } from '@jest/globals';
import fi from '@Language/fi.json';
import sv from '@Language/sv.json';

/**
 * Tests that all ET2026-specific field keys needed for muutoshistoria (change history)
 * have corresponding i18n translations.
 *
 * The change history view uses `propertyLabel` which resolves
 * flat keys like 'lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
 * by looking up `energiatodistus.lahtotiedot.energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin`
 * in the i18n locale files.
 */
describe('ET2026 change history i18n labels', () => {
  const et = fi.energiatodistus;

  describe('ET2026-specific lahtotiedot fields', () => {
    it('given fi.json, when checking for energiankulutuksen-valmius key, then it exists in lahtotiedot', () => {
      // given
      const lahtotiedot = et.lahtotiedot;

      // then
      expect(
        lahtotiedot[
          'energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
        ]
      ).toBeDefined();
    });

    it('given fi.json, when checking for lammonjako-lampotilajousto key, then it exists in lammitys', () => {
      // given
      const lammitys = et.lahtotiedot.lammitys;

      // then
      expect(lammitys['lammonjako-lampotilajousto']).toBeDefined();
    });
  });

  describe('ET2026-specific tulokset kokonaistuotanto fields', () => {
    it('given fi.json, when checking for uusiutuvat-omavaraisenergiat-kokonaistuotanto section, then it exists in tulokset', () => {
      // given
      const tulokset = et.tulokset;

      // then
      expect(
        tulokset['uusiutuvat-omavaraisenergiat-kokonaistuotanto']
      ).toBeDefined();
    });

    it('given fi.json, when checking for kokonaistuotanto sub-keys, then aurinkosahko, aurinkolampo, tuulisahko exist', () => {
      // given
      const kokonaistuotanto =
        et.tulokset['uusiutuvat-omavaraisenergiat-kokonaistuotanto'];

      // then
      expect(kokonaistuotanto).toBeDefined();
      expect(kokonaistuotanto.aurinkosahko).toBeDefined();
      expect(kokonaistuotanto.aurinkolampo).toBeDefined();
      expect(kokonaistuotanto.tuulisahko).toBeDefined();
      expect(kokonaistuotanto.lampopumppu).toBeDefined();
      expect(kokonaistuotanto.muulampo).toBeDefined();
      expect(kokonaistuotanto.muusahko).toBeDefined();
    });
  });

  describe('ET2026-specific toteutunut-ostoenergiankulutus fields', () => {
    it('given fi.json, when checking for tietojen-alkuperavuosi, then it exists', () => {
      // given
      const toteutunut = et['toteutunut-ostoenergiankulutus'];

      // then — this key already exists based on investigation
      expect(toteutunut['tietojen-alkuperavuosi']).toBeDefined();
    });

    it('given fi.json, when checking for uusiutuvat-polttoaineet-vuosikulutus-yhteensa, then it exists', () => {
      // given
      const toteutunut = et['toteutunut-ostoenergiankulutus'];

      // then
      expect(
        toteutunut['uusiutuvat-polttoaineet-vuosikulutus-yhteensa']
      ).toBeDefined();
    });

    it('given fi.json, when checking for fossiiliset-polttoaineet-vuosikulutus-yhteensa, then it exists', () => {
      // given
      const toteutunut = et['toteutunut-ostoenergiankulutus'];

      // then
      expect(
        toteutunut['fossiiliset-polttoaineet-vuosikulutus-yhteensa']
      ).toBeDefined();
    });

    it('given fi.json, when checking for uusiutuva-energia-vuosituotto-yhteensa, then it exists', () => {
      // given
      const toteutunut = et['toteutunut-ostoenergiankulutus'];

      // then
      expect(
        toteutunut['uusiutuva-energia-vuosituotto-yhteensa']
      ).toBeDefined();
    });
  });

  describe('Swedish translations for ET2026-specific change history keys', () => {
    it('given sv.json, when checking for energiankulutuksen-valmius key, then it exists in lahtotiedot', () => {
      // given
      const lahtotiedot = sv.energiatodistus.lahtotiedot;

      // then
      expect(
        lahtotiedot[
          'energiankulutuksen-valmius-reagoida-ulkoisiin-signaaleihin'
        ]
      ).toBeDefined();
    });

    it('given sv.json, when checking for lammonjako-lampotilajousto, then it exists in lammitys', () => {
      // given
      const lammitys = sv.energiatodistus.lahtotiedot.lammitys;

      // then
      expect(lammitys['lammonjako-lampotilajousto']).toBeDefined();
    });

    it('given sv.json, when checking for kokonaistuotanto section, then it exists in tulokset', () => {
      // given
      const tulokset = sv.energiatodistus.tulokset;

      // then
      expect(
        tulokset['uusiutuvat-omavaraisenergiat-kokonaistuotanto']
      ).toBeDefined();
    });
  });
});
