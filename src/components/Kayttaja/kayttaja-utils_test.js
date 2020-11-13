import { assert } from 'chai';
import * as KayttajaUtils from './kayttaja-utils';

describe('KayttajaUtils-suite: ', () => {
  describe('kayttajaHasAccessToResource', () => {
    it('should return true when given kayttaja has access', () => {
      const roolit = [1, 2, 3];
      const kayttaja = { rooli: 1 };

      assert.isTrue(
        KayttajaUtils.kayttajaHasAccessToResource(roolit, kayttaja)
      );
    });

    it('should return false when given kayttaja does not have access', () => {
      const roolit = [1, 2, 3];
      const kayttaja = { rooli: 4 };

      assert.isFalse(
        KayttajaUtils.kayttajaHasAccessToResource(roolit, kayttaja)
      );
    });
  });
});
