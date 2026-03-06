import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as fxmath from './fxmath';
import * as formats from './formats';

describe('Formats:', () => {
  describe('currencyFormat', () => {
    it('Using round is redundant', () => {
      expect(formats.currencyFormat(3.505)).toEqual('3,51');
      expect(R.compose(formats.currencyFormat, fxmath.round(2))(3.505)).toEqual(
        '3,51'
      );
    });
  });
  describe('numberFormatPrecision', () => {
    it('No rounding', () => {
      expect(formats.numberFormatPrecision(1, 0)).toEqual('0,0');
      expect(formats.numberFormatPrecision(1, 1.1)).toEqual('1,1');
      expect(formats.numberFormatPrecision(1, 1.1)).toEqual('1,1');
      expect(formats.numberFormatPrecision(1, 1.0)).toEqual('1,0');
      expect(formats.numberFormatPrecision(1, 2.0)).toEqual('2,0');
      expect(formats.numberFormatPrecision(1, 10.0)).toEqual('10,0');
    });

    it('Rounding n.05 (1 decimal)', () => {
      expect(formats.numberFormatPrecision(1, 1.05)).toEqual('1,1');
      expect(formats.numberFormatPrecision(1, 10.05)).toEqual('10,1');
      expect(formats.numberFormatPrecision(1, 100.05)).toEqual('100,1');
      expect(formats.numberFormatPrecision(1, 1000.05)).toEqual('1 000,1');
      expect(formats.numberFormatPrecision(1, 10000.05)).toEqual('10 000,1');
      expect(formats.numberFormatPrecision(1, 100000.05)).toEqual('100 000,1');
    });

    it('Rounding 3456.3456', () => {
      expect(formats.numberFormatPrecision(3, 3456.3456)).toEqual('3 456,346');
      expect(formats.numberFormatPrecision(2, 3456.3456)).toEqual('3 456,35');
      expect(formats.numberFormatPrecision(1, 3456.3456)).toEqual('3 456,3');
      expect(formats.numberFormatPrecision(0, 3456.3456)).toEqual('3 456');
    });

    it('Rounding with negative precision requires using round', () => {
      expect(() => formats.numberFormatPrecision(-1, 3456.3456)).toThrow();
      expect(
        R.compose(formats.numberFormat, fxmath.round(-1))(3456.3456)
      ).toEqual('3 460');
    });

    it('1.005 is rounded correctly even if round is not used', () => {
      expect(formats.currencyFormat(1.005)).toEqual('1,01');
      expect(R.compose(formats.currencyFormat, fxmath.round(2))(1.005)).toEqual(
        '1,01'
      );
    });

    it('Actual fixed bugs', () => {
      expect(formats.numberFormatPrecision(1, 71.05)).toEqual('71,1');
      expect(formats.numberFormatPrecision(1, 203 * 0.35)).toEqual('71,1');
    });
  });
});
