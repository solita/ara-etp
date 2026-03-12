// javascript
import { expect, describe, it } from '@jest/globals';
import fi from '@Language/fi.json';
import sv from '@Language/sv.json';

// --- Part 3: Localization Texts ---

describe('Ilmastoselvitys localization', () => {
  // Test 3.1: Finnish localization contains ilmastoselvitys section
  describe('Finnish localization contains ilmastoselvitys section', () => {
    it('given fi.json, when checking for ilmastoselvitys keys, then the section and sub-keys exist', () => {
      // given
      const ilmastoselvitys = fi.energiatodistus.ilmastoselvitys;

      // then
      expect(ilmastoselvitys).toBeDefined();
      expect(ilmastoselvitys.header).toBeDefined();
      expect(ilmastoselvitys.laatimisajankohta).toBeDefined();
      expect(ilmastoselvitys.laatija).toBeDefined();
      expect(ilmastoselvitys.yritys).toBeDefined();
      expect(ilmastoselvitys['yritys-osoite']).toBeDefined();
      expect(ilmastoselvitys['yritys-postinumero']).toBeDefined();
      expect(ilmastoselvitys['yritys-postitoimipaikka']).toBeDefined();
      expect(ilmastoselvitys.laadintaperuste).toBeDefined();
    });
  });

  // Test 3.2: Finnish localization contains hiilijalanjälki texts
  describe('Finnish localization contains hiilijalanjälki texts', () => {
    it('given fi.json, when checking for hiilijalanjalki keys, then the section and sub-keys exist', () => {
      // given
      const hiilijalanjalki =
        fi.energiatodistus.ilmastoselvitys.hiilijalanjalki;

      // then
      expect(hiilijalanjalki).toBeDefined();
      expect(hiilijalanjalki.header).toBeDefined();
      expect(hiilijalanjalki.rakennus).toBeDefined();
      expect(hiilijalanjalki.rakennuspaikka).toBeDefined();
      expect(hiilijalanjalki.yhteensa).toBeDefined();
      expect(hiilijalanjalki['rakennustuotteiden-valmistus']).toBeDefined();
      expect(hiilijalanjalki['kuljetukset-tyomaavaihe']).toBeDefined();
      expect(hiilijalanjalki['rakennustuotteiden-vaihdot']).toBeDefined();
      expect(hiilijalanjalki.energiankaytto).toBeDefined();
      expect(hiilijalanjalki.purkuvaihe).toBeDefined();
    });
  });

  // Test 3.3: Finnish localization contains hiilikädenjälki texts
  describe('Finnish localization contains hiilikädenjälki texts', () => {
    it('given fi.json, when checking for hiilikadenjalki keys, then the section and sub-keys exist', () => {
      // given
      const hiilikadenjalki =
        fi.energiatodistus.ilmastoselvitys.hiilikadenjalki;

      // then
      expect(hiilikadenjalki).toBeDefined();
      expect(hiilikadenjalki.header).toBeDefined();
      expect(hiilikadenjalki.uudelleenkaytto).toBeDefined();
      expect(hiilikadenjalki.kierratys).toBeDefined();
      expect(hiilikadenjalki['ylimaarainen-uusiutuvaenergia']).toBeDefined();
      expect(hiilikadenjalki.hiilivarastovaikutus).toBeDefined();
      expect(hiilikadenjalki.karbonatisoituminen).toBeDefined();
    });
  });

  // Test 3.4: Swedish localization contains placeholder texts for new keys
  describe('Swedish localization contains placeholder texts for new keys', () => {
    it('given sv.json, when checking for ilmastoselvitys keys, then the same new keys exist as in fi.json', () => {
      // given
      const svIlmastoselvitys = sv.energiatodistus.ilmastoselvitys;

      // then
      expect(svIlmastoselvitys).toBeDefined();
      expect(svIlmastoselvitys.header).toBeDefined();
      expect(svIlmastoselvitys.laatimisajankohta).toBeDefined();
      expect(svIlmastoselvitys.laatija).toBeDefined();
      expect(svIlmastoselvitys.yritys).toBeDefined();
      expect(svIlmastoselvitys['yritys-osoite']).toBeDefined();
      expect(svIlmastoselvitys['yritys-postinumero']).toBeDefined();
      expect(svIlmastoselvitys['yritys-postitoimipaikka']).toBeDefined();
      expect(svIlmastoselvitys.laadintaperuste).toBeDefined();
    });

    it('given sv.json, when checking hiilijalanjalki keys, then the same new keys exist', () => {
      // given
      const svHiilijalanjalki =
        sv.energiatodistus.ilmastoselvitys.hiilijalanjalki;

      // then
      expect(svHiilijalanjalki).toBeDefined();
      expect(svHiilijalanjalki.header).toBeDefined();
    });

    it('given sv.json, when checking hiilikadenjalki keys, then the same new keys exist', () => {
      // given
      const svHiilikadenjalki =
        sv.energiatodistus.ilmastoselvitys.hiilikadenjalki;

      // then
      expect(svHiilikadenjalki).toBeDefined();
      expect(svHiilikadenjalki.header).toBeDefined();
    });

    it('given sv.json, when checking ilmastoselvitys values, then they follow the placeholder convention', () => {
      // given
      const svIlmastoselvitys = sv.energiatodistus.ilmastoselvitys;

      // then — Swedish placeholders should follow the "(sv)" convention
      expect(svIlmastoselvitys.header).toMatch(/\(sv\)$/);
    });
  });
});
