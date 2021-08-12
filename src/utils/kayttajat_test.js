import { assert } from 'chai';
import * as Kayttajat from './kayttajat';

describe('Kayttajat', () => {
  describe('Roles', () => {
    it('should return true for laatija', () => {
      const kayttaja = { rooli: 0 };
      assert.isTrue(Kayttajat.isLaatija(kayttaja));
    });
    it('should return false for other than laatija', () => {
      const kayttaja = { rooli: 1 };
      assert.isFalse(Kayttajat.isLaatija(kayttaja));
    });
    it('should return true for patevyydentoteaja', () => {
      const kayttaja = { rooli: 1 };
      assert.isTrue(Kayttajat.isPatevyydentoteaja(kayttaja));
    });
    it('should return false for other than patevyydentoteaja', () => {
      const kayttaja = { rooli: 0 };
      assert.isFalse(Kayttajat.isPatevyydentoteaja(kayttaja));
    });
    it('should return true for paakayttaja', () => {
      const kayttaja = { rooli: 2 };
      assert.isTrue(Kayttajat.isPaakayttaja(kayttaja));
    });
    it('should return false for other than paakayttaja', () => {
      const kayttaja = { rooli: 0 };
      assert.isFalse(Kayttajat.isPaakayttaja(kayttaja));
    });
    it('should return true for laskuttaja', () => {
      const kayttaja = { rooli: 3 };
      assert.isTrue(Kayttajat.isLaskuttaja(kayttaja));
    });
    it('should return false for other than laskuttaja', () => {
      const kayttaja = { rooli: 0 };
      assert.isFalse(Kayttajat.isLaskuttaja(kayttaja));
    });

    it('should return true for paakayttaja or laskuttaja', () => {
      assert.isTrue(Kayttajat.isPaakayttajaOrLaskuttaja({ rooli: 2 }));
      assert.isTrue(Kayttajat.isPaakayttajaOrLaskuttaja({ rooli: 3 }));
    });
  });
  describe('Name formatting', () => {
    it('should return formatted full name', () => {
      const kayttaja = { etunimi: 'John', sukunimi: 'Doe' };
      assert.equal(Kayttajat.fullName(kayttaja), 'John Doe');
    });
  });
});
