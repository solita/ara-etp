import { assert } from 'chai';
import * as validation from './validation';

describe('Validation:', () => {
  describe('ytunnus validation', () => {

    function assertChecksum(ytunnus, checksum) {
      assert.equal(validation.ytunnusChecksum(ytunnus), checksum);
    }

    function assertIsValid(ytunnus) {
      assert.equal(validation.validYtunnus(ytunnus), true);
    }

    function assertIsInvalid(ytunnus) {
      assert.equal(validation.validYtunnus(ytunnus), false);
    }

    it('ytunnus checksum', () => {
      assertChecksum("0000000", 0);
      assertChecksum("0000001", 9);
      assertChecksum("0000002", 7);
      assertChecksum("1234567", 1);
    });

    it('valid ytunnus', () => {
      assertIsValid("0000000-0");
      assertIsValid("0000001-9");
      assertIsValid("1234567-1");
    });

    it('invalid ytunnus - invalid checksum', () => {
      assertIsInvalid("0000000-1");
      assertIsInvalid("0000001-1");
    });

    it('invalid ytunnus - contains not numbers', () => {
      assertIsInvalid("a000000-1");
      assertIsInvalid("0000001-a");
      assertIsInvalid("a");
    });
  });
});
