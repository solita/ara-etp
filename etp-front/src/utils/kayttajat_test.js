import { expect, describe, it } from '@jest/globals';
import * as Kayttajat from './kayttajat';

describe('Kayttajat', () => {
  describe('Roles', () => {
    it('should return true for laatija', () => {
      const kayttaja = { rooli: 0 };
      expect(Kayttajat.isLaatija(kayttaja)).toBe(true);
    });
    it('should return false for other than laatija', () => {
      const kayttaja = { rooli: 1 };
      expect(Kayttajat.isLaatija(kayttaja)).toBe(false);
    });
    it('should return true for patevyydentoteaja', () => {
      const kayttaja = { rooli: 1 };
      expect(Kayttajat.isPatevyydentoteaja(kayttaja)).toBe(true);
    });
    it('should return false for other than patevyydentoteaja', () => {
      const kayttaja = { rooli: 0 };
      expect(Kayttajat.isPatevyydentoteaja(kayttaja)).toBe(false);
    });
    it('should return true for paakayttaja', () => {
      const kayttaja = { rooli: 2 };
      expect(Kayttajat.isPaakayttaja(kayttaja)).toBe(true);
    });
    it('should return false for other than paakayttaja', () => {
      const kayttaja = { rooli: 0 };
      expect(Kayttajat.isPaakayttaja(kayttaja)).toBe(false);
    });
    it('should return true for laskuttaja', () => {
      const kayttaja = { rooli: 3 };
      expect(Kayttajat.isLaskuttaja(kayttaja)).toBe(true);
    });
    it('should return false for other than laskuttaja', () => {
      const kayttaja = { rooli: 0 };
      expect(Kayttajat.isLaskuttaja(kayttaja)).toBe(false);
    });

    it('should return true for paakayttaja or laskuttaja', () => {
      expect(Kayttajat.isPaakayttajaOrLaskuttaja({ rooli: 2 })).toBe(true);
      expect(Kayttajat.isPaakayttajaOrLaskuttaja({ rooli: 3 })).toBe(true);
    });
  });
  describe('Name formatting', () => {
    it('should return formatted full name', () => {
      const kayttaja = { etunimi: 'John', sukunimi: 'Doe' };
      expect(Kayttajat.fullName(kayttaja)).toEqual('John Doe');
    });
  });
});
