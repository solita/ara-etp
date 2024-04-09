import { expect, describe, it } from '@jest/globals';

import * as ToimintaAlueUtils from './toimintaalue';
import * as Maybe from '@Utility/maybe-utils';

describe('ToimintaAlueUtils', () => {
  describe('toimintaalueetWithoutMain', () => {
    it('should return toimintaAlueet without mainToimintaalue', () => {
      const toimintaAlueet = [1, 2, 3, 4, 5];
      const mainToimintaAlue = Maybe.of(1);

      const expected = [2, 3, 4, 5];

      expect(expected).toEqual(
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

      expect(expected).toEqual(
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

      expect(expected).toEqual(
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

      expect(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      ).toBe(true);
    });

    it('should return false with inequal toimintaAlue', () => {
      const mainToimintaAlue = Maybe.of(1);
      const toimintaAlue = 2;

      expect(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      ).toBe(false);
    });

    it('should return false with None mainToimintaAlue', () => {
      const mainToimintaAlue = Maybe.None();
      const toimintaAlue = 2;

      expect(
        ToimintaAlueUtils.isMainToimintaAlue(mainToimintaAlue, toimintaAlue)
      ).toBe(false);
    });
  });
});
