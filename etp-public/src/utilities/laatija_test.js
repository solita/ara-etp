import { expect, describe, it } from '@jest/globals';
import * as Laatija from './laatija';

describe('laatija', () => {
  describe('painotus', () => {
    describe('weightByToimintaalue', () => {
      it('should return 2 with main toimintaalue', () => {
        expect(Laatija.weightByToimintaalueet(new Set([1]), 1)).toEqual(2);
      });

      it('should return 0 with not main toimintaalue', () => {
        expect(Laatija.weightByToimintaalueet(new Set([2]), 1)).toEqual(0);
      });
    });

    describe('weightByMuuToimintaalue', () => {
      it('should return 1 with muu toimintaalue', () => {
        expect(
          Laatija.weightByMuutToimintaalueet(new Set([2]), [2, 3, 4])
        ).toEqual(1);
      });

      it('should return 0 with not muu toimintaalue', () => {
        expect(
          Laatija.weightByMuutToimintaalueet(new Set([1]), [2, 3, 4])
        ).toEqual(0);
      });
    });

    describe('weightByJulkisettiedot', () => {
      it('should return 2 with any flag true', () => {
        expect(Laatija.weightByJulkisettiedot(true, true, true)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(true, true, false)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(true, false, true)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(true, false, false)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(false, true, true)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(false, true, false)).toEqual(2);
        expect(Laatija.weightByJulkisettiedot(false, false, true)).toEqual(2);
      });

      it('should return 0 when all flags false', () => {
        expect(Laatija.weightByJulkisettiedot(false, false, false)).toEqual(0);
      });
    });
  });
});
