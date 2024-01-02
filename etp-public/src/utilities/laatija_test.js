import { assert } from 'chai';
import * as Laatija from './laatija';

describe('laatija', () => {
  describe('painotus', () => {
    describe('weightByToimintaalue', () => {
      it('should return 2 with main toimintaalue', () => {
        assert.equal(Laatija.weightByToimintaalueet(new Set([1]), 1), 2);
      });

      it('should return 0 with not main toimintaalue', () => {
        assert.equal(Laatija.weightByToimintaalueet(new Set([2]), 1), 0);
      });
    });

    describe('weightByMuuToimintaalue', () => {
      it('should return 1 with muu toimintaalue', () => {
        assert.equal(
          Laatija.weightByMuutToimintaalueet(new Set([2]), [2, 3, 4]),
          1
        );
      });

      it('should return 0 with not muu toimintaalue', () => {
        assert.equal(
          Laatija.weightByMuutToimintaalueet(new Set([1]), [2, 3, 4]),
          0
        );
      });
    });

    describe('weightByJulkisettiedot', () => {
      it('should return 2 with any flag true', () => {
        assert.equal(Laatija.weightByJulkisettiedot(true, true, true), 2);
        assert.equal(Laatija.weightByJulkisettiedot(true, true, false), 2);
        assert.equal(Laatija.weightByJulkisettiedot(true, false, true), 2);
        assert.equal(Laatija.weightByJulkisettiedot(true, false, false), 2);
        assert.equal(Laatija.weightByJulkisettiedot(false, true, true), 2);
        assert.equal(Laatija.weightByJulkisettiedot(false, true, false), 2);
        assert.equal(Laatija.weightByJulkisettiedot(false, false, true), 2);
      });

      it('should return 0 when all flags false', () => {
        assert.equal(Laatija.weightByJulkisettiedot(false, false, false), 0);
      });
    });

    describe('weightByActivity', () => {
      it('should return 1 when last login is under half years past', () => {
        const datenow = new Date('2020-10-01T00:00:00.00000Z');
        const login = new Date('2020-09-30T00:00:00.00000Z');

        assert.equal(Laatija.weightByActivity(datenow, login), 1);
      });

      it('should return 1 when last login is half year past exact', () => {
        const datenow = new Date('2020-10-01T00:00:00.00000Z');
        const login = new Date('2020-04-01T00:00:01.000Z');

        assert.equal(Laatija.weightByActivity(datenow, login), 1);
      });

      it('should return 0 when last login is over half years past', () => {
        const datenow = new Date('2020-10-01T00:00:00.00000Z');
        const login = new Date('2020-01-01T00:00:00.00000Z');

        assert.equal(Laatija.weightByActivity(datenow, login), 0);
      });
    });
  });
});
