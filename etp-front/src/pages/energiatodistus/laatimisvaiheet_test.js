import { expect, describe, it } from '@jest/globals';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';

import * as Laatimisvaiheet from './laatimisvaiheet';

const et2018inVaihe = vaihe => ({
  versio: 2018,
  perustiedot: {
    laatimisvaihe: Maybe.Some(vaihe)
  }
});

const et2026inVaihe = vaihe => ({
  versio: 2026,
  perustiedot: {
    laatimisvaihe: Maybe.Some(vaihe)
  }
});

const et2018UnknownVaihe = {
  versio: 2018,
  perustiedot: {
    laatimisvaihe: Maybe.None()
  }
};

const et2026UnknownVaihe = {
  versio: 2026,
  perustiedot: {
    laatimisvaihe: Maybe.None()
  }
};

const et2013 = uudisrakennus => ({
  versio: 2013,
  perustiedot: {
    uudisrakennus: uudisrakennus
  }
});

describe('Laatimisvaiheet: ', () => {
  // --- Regression tests (existing functionality) ---

  it('Olemassaoleva rakennus - 2018', () => {
    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(2))).toEqual(
      true
    );

    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(1))).toEqual(
      false
    );

    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(0))).toEqual(
      false
    );

    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018UnknownVaihe)).toEqual(
      false
    );
  });

  it('Olemassaoleva rakennus - 2013', () => {
    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2013(false))).toEqual(
      true
    );

    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2013(true))).toEqual(
      false
    );
  });

  // --- Regression: isOlemassaOlevaRakennus returns false for new IDs 3, 4 ---

  it('Olemassaoleva rakennus - returns false for new IDs 3 and 4 (regression)', () => {
    // given a 2018 ET with laatimisvaihe 3, then isOlemassaOlevaRakennus is false
    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(3))).toEqual(
      false
    );
    // given a 2018 ET with laatimisvaihe 4, then isOlemassaOlevaRakennus is false
    expect(Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(4))).toEqual(
      false
    );
  });

  // --- NEW: isRakennuslupaPerusparannus ---

  it('isRakennuslupaPerusparannus returns true for vaihe 3', () => {
    // given a 2026 ET with laatimisvaihe = 3
    // when checking isRakennuslupaPerusparannus
    // then true is returned
    expect(
      Laatimisvaiheet.isRakennuslupaPerusparannus(et2026inVaihe(3))
    ).toEqual(true);
  });

  it('isRakennuslupaPerusparannus returns false for vaihe 0, 1, 2, 4', () => {
    // given ETs with laatimisvaihe 0, 1, 2, 4
    // when checking isRakennuslupaPerusparannus
    // then false is returned for all
    [0, 1, 2, 4].forEach(id => {
      expect(
        Laatimisvaiheet.isRakennuslupaPerusparannus(et2026inVaihe(id))
      ).toEqual(false);
    });
  });

  // --- NEW: isKayttoonottoPerusparannus ---

  it('isKayttoonottoPerusparannus returns true for vaihe 4', () => {
    // given a 2026 ET with laatimisvaihe = 4
    // when checking isKayttoonottoPerusparannus
    // then true is returned
    expect(
      Laatimisvaiheet.isKayttoonottoPerusparannus(et2026inVaihe(4))
    ).toEqual(true);
  });

  it('isKayttoonottoPerusparannus returns false for vaihe 0, 1, 2, 3', () => {
    // given ETs with laatimisvaihe 0, 1, 2, 3
    // when checking isKayttoonottoPerusparannus
    // then false is returned for all
    [0, 1, 2, 3].forEach(id => {
      expect(
        Laatimisvaiheet.isKayttoonottoPerusparannus(et2026inVaihe(id))
      ).toEqual(false);
    });
  });

  // --- NEW: isHavainnointikayntiRequired ---

  it('isHavainnointikayntiRequired returns true for vaihe 2, 3, 4', () => {
    // given ETs with laatimisvaihe 2, 3, 4
    // when checking isHavainnointikayntiRequired
    // then true is returned for all three
    [2, 3, 4].forEach(id => {
      expect(
        Laatimisvaiheet.isHavainnointikayntiRequired(et2026inVaihe(id))
      ).toEqual(true);
    });
  });

  it('isHavainnointikayntiRequired returns false for vaihe 0, 1', () => {
    // given ETs with laatimisvaihe 0, 1
    // when checking isHavainnointikayntiRequired
    // then false is returned for both
    [0, 1].forEach(id => {
      expect(
        Laatimisvaiheet.isHavainnointikayntiRequired(et2026inVaihe(id))
      ).toEqual(false);
    });
  });

  it('isHavainnointikayntiRequired returns false for None laatimisvaihe', () => {
    // given a 2026 ET with no laatimisvaihe set
    // when checking isHavainnointikayntiRequired
    // then false is returned
    expect(
      Laatimisvaiheet.isHavainnointikayntiRequired(et2026UnknownVaihe)
    ).toEqual(false);
  });

  // --- NEW: isUudisrakennus for all IDs ---

  it('isUudisrakennus returns true for vaihe 0 and 1', () => {
    // given ETs with laatimisvaihe 0 (rakennuslupa) or 1 (kayttoonotto)
    // when checking isUudisrakennus
    // then true is returned
    [0, 1].forEach(id => {
      expect(Laatimisvaiheet.isUudisrakennus(et2018inVaihe(id))).toEqual(true);
    });
  });

  it('isUudisrakennus returns false for vaihe 2, 3, 4', () => {
    // given ETs with laatimisvaihe 2, 3, or 4
    // when checking isUudisrakennus
    // then false is returned
    [2, 3, 4].forEach(id => {
      expect(Laatimisvaiheet.isUudisrakennus(et2026inVaihe(id))).toEqual(false);
    });
  });
});
