import { expect, describe, it } from '@jest/globals';
import * as R from 'ramda';
import * as dfns from 'date-fns';
import * as validation from './validation';

const assertChecksum = R.curry((value, checksum, validator) => {
  expect(validator(value)).toEqual(checksum);
});

const assertIsValid = R.curry((value, validator) =>
  expect(validator(value)).toEqual(true)
);

const assertIsInvalid = R.curry((value, validator) =>
  expect(validator(value)).toEqual(false)
);

describe('Validation:', () => {
  describe('ytunnus validation', () => {
    const assertYtunnusChecksum = (value, checksum) =>
      assertChecksum(value, checksum)(validation.ytunnusChecksum);

    const assertYtunnusIsValid = value =>
      assertIsValid(value)(validation.isValidYtunnus);

    const assertYtunnusIsInvalid = value =>
      assertIsInvalid(value)(validation.isValidYtunnus);

    it('ytunnus checksum', () => {
      assertYtunnusChecksum('0000000', 0);
      assertYtunnusChecksum('0000001', 9);
      assertYtunnusChecksum('0000002', 7);
      assertYtunnusChecksum('1234567', 1);
      assertYtunnusChecksum('0010001', 10);
    });

    it('valid ytunnus', () => {
      assertYtunnusIsValid('0000000-0');
      assertYtunnusIsValid('0000001-9');
      assertYtunnusIsValid('1234567-1');
    });

    it('invalid ytunnus - invalid checksum', () => {
      assertYtunnusIsInvalid('0000000-1');
      assertYtunnusIsInvalid('0000001-1');
    });

    it('invalid ytunnus - contains not numbers', () => {
      assertYtunnusIsInvalid('a000000-1');
      assertYtunnusIsInvalid('0000001-a');
      assertYtunnusIsInvalid('a');
    });

    it('invalid ytunnus - checksum 10', () => {
      assertYtunnusIsInvalid('0010001-1');
      assertYtunnusIsInvalid('0010001-10');
    });
  });

  describe('Henkilötunnus validation', () => {
    const assertHenkilotunnusChecksum = (value, checksum) =>
      assertChecksum(value, checksum)(validation.henkilotunnusChecksum);

    const assertHenkilotunnusIsValid = value =>
      assertIsValid(value)(validation.isValidHenkilotunnus);

    const assertHenkilotunnusIsInvalid = value =>
      assertIsInvalid(value)(validation.isValidHenkilotunnus);

    it('henkilötunnus checksum', () => {
      assertHenkilotunnusChecksum('131052-308T', 'T');
      assertHenkilotunnusChecksum('130200A892S', 'S');
      assertHenkilotunnusChecksum('130200A892s', 'S');
    });

    it('valid henkilötunnus', () => {
      assertHenkilotunnusIsValid('131052-308T');
      assertHenkilotunnusIsValid('130200A892S');
      assertHenkilotunnusIsValid('130200X892S');

      // Henkilotunnukset from DVV example
      // Contains all the new century signs
      assertHenkilotunnusIsValid('010594Y9032');
      assertHenkilotunnusIsValid('010594Y9021');
      assertHenkilotunnusIsValid('020594X903P');
      assertHenkilotunnusIsValid('020594X902N');
      assertHenkilotunnusIsValid('030594W903B');
      assertHenkilotunnusIsValid('030694W9024');
      assertHenkilotunnusIsValid('040594V9030');
      assertHenkilotunnusIsValid('040594V902Y');
      assertHenkilotunnusIsValid('050594U903M');
      assertHenkilotunnusIsValid('050594U902L');
      assertHenkilotunnusIsValid('010516B903X');
      assertHenkilotunnusIsValid('010516B902W');
      assertHenkilotunnusIsValid('020516C903K');
      assertHenkilotunnusIsValid('020516C902J');
      assertHenkilotunnusIsValid('030516D9037');
      assertHenkilotunnusIsValid('030516D9026');
      assertHenkilotunnusIsValid('010501E9032');
      assertHenkilotunnusIsValid('020502E902X');
      assertHenkilotunnusIsValid('020503F9037');
      assertHenkilotunnusIsValid('020504A902E');
      assertHenkilotunnusIsValid('020504B904H');
      assertHenkilotunnusIsValid('100190-999P');
      assertHenkilotunnusIsValid('100190Y999P');

      // Test for + also
      assertHenkilotunnusIsValid('130299+892A');

      // Valid leap day
      assertHenkilotunnusIsValid('290292-0002');
    });

    it('invalid henkilötunnus', () => {
      assertHenkilotunnusIsInvalid(null);
      assertHenkilotunnusIsInvalid('131052B308T');
      assertHenkilotunnusIsInvalid('131053-308T');
      assertHenkilotunnusIsInvalid('0131053-308T');

      assertHenkilotunnusIsInvalid('130200A891S');
      assertHenkilotunnusIsInvalid('1A0200A892S');

      assertHenkilotunnusIsInvalid('130200A892s');
      assertHenkilotunnusIsInvalid('130200a892s');

      assertHenkilotunnusIsInvalid('310292-123A');

      assertHenkilotunnusIsInvalid('100190T999P');
      assertHenkilotunnusIsInvalid('100190Z999P');
      assertHenkilotunnusIsInvalid('100190G999P');

      // Invalid leap day
      assertHenkilotunnusIsInvalid('290201-000K');
    });
  });

  describe('Laatijan toteaja validation', () => {
    it('valid laatijan toteaja', () => {
      expect(validation.isLaatijanToteaja('FISE')).toEqual(true);
      expect(validation.isLaatijanToteaja('KIINKO')).toEqual(true);
    });

    it('invalid laatijan toteaja', () => {
      expect(validation.isLaatijanToteaja('F')).toEqual(false);
      expect(validation.isLaatijanToteaja(null)).toEqual(false);
    });
  });

  describe('Puhelin validation', () => {
    it('valid puhelin', () => {
      expect(validation.isPuhelin('04001234567')).toEqual(true);
      expect(validation.isPuhelin('0312345')).toEqual(true);
    });

    it('invalid puhelin', () => {
      expect(validation.isPuhelin('aa333')).toEqual(false);
      expect(validation.isPuhelin(null)).toEqual(false);
    });
  });

  describe('Pätevyystaso validation', () => {
    it('valid pätevyystaso', () => {
      expect(validation.isPatevyystaso('1')).toEqual(true);
      expect(validation.isPatevyystaso('2')).toEqual(true);
      expect(validation.isPatevyystaso(1)).toEqual(true);
      expect(validation.isPatevyystaso(2)).toEqual(true);
    });

    it('invalid pätevyystaso', () => {
      expect(validation.isPatevyystaso('3')).toEqual(false);
      expect(validation.isPatevyystaso(null)).toEqual(false);
    });
  });

  describe('Päivämäärä validation', () => {
    it('valid päivämäärä', () => {
      expect(validation.isPaivamaara('16.12.2012')).toEqual(true);
      expect(validation.isPaivamaara('01.12.2012')).toEqual(true);
      expect(
        validation.isPaivamaara(
          dfns.parse('16.12.2012', validation.DATE_FORMAT, 0)
        )
      ).toEqual(true);
      expect(
        validation.isPaivamaara(
          dfns.parse('01.12.2012', validation.DATE_FORMAT, 0)
        )
      ).toEqual(true);
    });

    it('invalid päivämäärä', () => {
      expect(validation.isPaivamaara('30.02.1992')).toEqual(false);
      expect(validation.isPaivamaara('1.20.2012')).toEqual(false);
      expect(validation.isPaivamaara('77.1.2012')).toEqual(false);
      expect(validation.isPaivamaara('77.1.20122')).toEqual(false);
      expect(validation.isPaivamaara(null)).toEqual(false);
    });
  });

  describe('Rakennustunnus validation', () => {
    it('valid rakennustunnus', () => {
      expect(validation.isRakennustunnus('1035150826')).toEqual(true);
      expect(validation.isRakennustunnus('103515074X')).toEqual(true);
    });

    it('invalid rakennustunnus', () => {
      expect(validation.isRakennustunnus('100012345A')).toEqual(false);
      expect(validation.isRakennustunnus('103515074x')).toEqual(false);
      expect(validation.isRakennustunnus(null)).toEqual(false);
    });
  });

  describe('OVT-tunnus validation', () => {
    it('valid OVT-tunnus', () => {
      expect(validation.isOVTTunnus('003712345671')).toEqual(true);
      expect(validation.isOVTTunnus('0037123456710')).toEqual(true);
      expect(validation.isOVTTunnus('00371234567101')).toEqual(true);
      expect(validation.isOVTTunnus('003712345671012')).toEqual(true);
      expect(validation.isOVTTunnus('0037123456710123')).toEqual(true);
      expect(validation.isOVTTunnus('00371234567101234')).toEqual(true);
    });

    it('invalid OVT-tunnus', () => {
      expect(validation.isOVTTunnus('003712345671012345')).toEqual(false);
      expect(validation.isOVTTunnus('000012345671')).toEqual(false);
      expect(validation.isOVTTunnus(null)).toEqual(false);
      expect(validation.isOVTTunnus('')).toEqual(false);
      expect(validation.isOVTTunnus('test@ara.fi')).toEqual(false);
    });
  });

  describe('IBAN validation', () => {
    it('valid IBAN', () => {
      expect(validation.isIBAN('FI1410093000123458')).toBe(true);
      expect(validation.isIBAN('BR1500000000000010932840814P2')).toBe(true);
      expect(validation.isIBAN('GB82WEST12345698765432')).toBe(true);
      expect(validation.isIBAN('BE71096123456769')).toBe(true);
      expect(validation.isIBAN('BR1500000000000010932840814P2')).toBe(true);
    });

    it('invalid IBAN', () => {
      expect(validation.isIBAN('003712345671')).toBe(false);
      expect(validation.isIBAN('FI1410093000123459')).toBe(false);
      expect(validation.isIBAN('FI14')).toBe(false);
      expect(validation.isIBAN(null)).toBe(false);
      expect(validation.isIBAN('')).toBe(false);
      expect(validation.isIBAN('test@ara.fi')).toBe(false);
      expect(validation.isIBAN('*?+2#/="')).toBe(false);
    });
  });

  describe('TEOVT-tunnus validation', () => {
    it('valid TEOVT-tunnus', () => {
      expect(validation.isTEOVTTunnus('TE003712345671')).toEqual(true);
      expect(validation.isTEOVTTunnus('TE0037123456710')).toEqual(true);
    });

    it('invalid TEOVT-tunnus', () => {
      expect(validation.isTEOVTTunnus('003712345671')).toEqual(false);
      expect(validation.isTEOVTTunnus('TE003712345672')).toEqual(false);
      expect(validation.isTEOVTTunnus(null)).toEqual(false);
      expect(validation.isTEOVTTunnus('')).toEqual(false);
    });
  });
});
