import { assert } from 'chai';
import * as R from 'ramda';
import moment from 'moment';
import * as validation from './validation';

const assertChecksum = R.curry((value, checksum, validator) => {
  assert.equal(validator(value), checksum);
});

const assertIsValid = R.curry((value, validator) =>
  assert.equal(validator(value), true)
);

const assertIsInvalid = R.curry((value, validator) =>
  assert.equal(validator(value), false)
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
      assertHenkilotunnusChecksum('131052-308T', 't');
      assertHenkilotunnusChecksum('130200A892S', 's');
      assertHenkilotunnusChecksum('130200a892s', 's');
    });

    it('valid henkilötunnus', () => {
      assertHenkilotunnusIsValid('131052-308T');
      assertHenkilotunnusIsValid('130200A892S');
      assertHenkilotunnusIsValid('130200a892s');
    });

    it('invalid henkilötunnus', () => {
      assertHenkilotunnusIsInvalid('131052B308T');
      assertHenkilotunnusIsInvalid('131053-308T');
      assertHenkilotunnusIsInvalid('0131053-308T');

      assertHenkilotunnusIsInvalid('130200X892S');
      assertHenkilotunnusIsInvalid('130200A891S');
      assertHenkilotunnusIsInvalid('1A0200A892S');
    });
  });

  describe('Laatijan toteaja validation', () => {
    it('valid laatijan toteaja', () => {
      assert.equal(validation.isLaatijanToteaja('FISE'), true);
      assert.equal(validation.isLaatijanToteaja('KIINKO'), true);
    });

    it('invalid laatijan toteaja', () => {
      assert.equal(validation.isLaatijanToteaja('F'), false);
      assert.equal(validation.isLaatijanToteaja(null), false);
    });
  });

  describe('Puhelin validation', () => {
    it('valid puhelin', () => {
      assert.equal(validation.isPuhelin('04001234567'), true);
      assert.equal(validation.isPuhelin('0312345'), true);
    });

    it('invalid puhelin', () => {
      assert.equal(validation.isPuhelin('aa333'), false);
      assert.equal(validation.isPuhelin(null), false);
    });
  });

  describe('Pätevyystaso validation', () => {
    it('valid pätevyystaso', () => {
      assert.equal(validation.isPatevyystaso('1'), true);
      assert.equal(validation.isPatevyystaso('2'), true);
      assert.equal(validation.isPatevyystaso(1), true);
      assert.equal(validation.isPatevyystaso(2), true);
    });

    it('invalid pätevyystaso', () => {
      assert.equal(validation.isPatevyystaso('3'), false);
      assert.equal(validation.isPatevyystaso(null), false);
    });
  });

  describe('Päivämäärä validation', () => {
    it('valid päivämäärä', () => {
      assert.equal(validation.isPaivamaara('16.12.2012'), true);
      assert.equal(validation.isPaivamaara('01.12.2012'), true);
      assert.equal(
        validation.isPaivamaara(
          moment('16.12.2012', validation.DATE_FORMAT).toDate()
        ),
        true
      );
      assert.equal(
        validation.isPaivamaara(
          moment('01.12.2012', validation.DATE_FORMAT).toDate()
        ),
        true
      );
    });

    it('invalid päivämäärä', () => {
      assert.equal(validation.isPaivamaara('1.20.2012'), false);
      assert.equal(validation.isPaivamaara('77.1.2012'), false);
      assert.equal(validation.isPaivamaara('77.1.20122'), false);
      assert.equal(validation.isPaivamaara(null), false);
    });
  });
});
