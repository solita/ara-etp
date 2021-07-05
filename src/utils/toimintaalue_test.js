import { assert } from 'chai';

import * as ToimintaAlueUtils from './toimintaalue';
import * as Maybe from '@Utility/maybe-utils';

describe('ToimintaAlueUtils', () => {
  describe('toimintaalueetWithoutMain', () => {
    it('should return toimintaAlueet without mainToimintaalue', () => {
      const toimintaAlueet = [1, 2, 3, 4, 5];
      const mainToimintaAlue = Maybe.of(1);

      const expected = [2, 3, 4, 5];

      assert.deepEqual(
        expected,
        ToimintaAlueUtils.toimintaalueetWithoutMain(
          mainToimintaAlue,
          toimintaAlueet
        )
      );
    });

    it('should return identity when called with None mainToimintaAlue', () => {
      const toimintaAlueet = [1, 2, 3, 4, 5];
      const mainToimintaAlue = Maybe.None();

      const expected = [1, 2, 3, 4, 5];

      assert.deepEqual(
        expected,
        ToimintaAlueUtils.toimintaalueetWithoutMain(
          mainToimintaAlue,
          toimintaAlueet
        )
      );
    });

    it('should return identity when called with mainToimintaAlue not in toimintaAlue-list', () => {
      const toimintaAlueet = [1, 2, 3, 4, 5];
      const mainToimintaAlue = Maybe.of(6);

      const expected = [1, 2, 3, 4, 5];

      assert.deepEqual(
        expected,
        ToimintaAlueUtils.toimintaalueetWithoutMain(
          mainToimintaAlue,
          toimintaAlueet
        )
      );
    });
  });

  describe('isMainToimintaAlue', () => {
    it('should return true with equal toimintaAlue', () => {
      const mainToimintaAlue = Maybe.of(1);
      const toimintaAlue = 1;

      assert.isTrue(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      );
    });

    it('should return false with inequal toimintaAlue', () => {
      const mainToimintaAlue = Maybe.of(1);
      const toimintaAlue = 2;

      assert.isFalse(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      );
    });

    it('should return false with None mainToimintaAlue', () => {
      const mainToimintaAlue = Maybe.None();
      const toimintaAlue = 2;

      assert.isFalse(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      );
    });
  });
});
