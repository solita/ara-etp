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

const et2018UnknownVaihe = {
  versio: 2018,
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
});

const allLaatimisvaiheet = [
  { id: 0, 'label-fi': 'A' },
  { id: 1, 'label-fi': 'B' },
  { id: 2, 'label-fi': 'C' },
  { id: 3, 'label-fi': 'D' },
  { id: 4, 'label-fi': 'E' }
];

describe('filterByVersion: ', () => {
  it('returns only ids 0, 1, 2 for version 2018', () => {
    const result = Laatimisvaiheet.filterByVersion(2018, allLaatimisvaiheet);
    expect(result.map(v => v.id)).toEqual([0, 1, 2]);
  });

  it('returns all ids for version 2026', () => {
    const result = Laatimisvaiheet.filterByVersion(2026, allLaatimisvaiheet);
    expect(result.map(v => v.id)).toEqual([0, 1, 2, 3, 4]);
  });

  it('returns empty array for version 2013', () => {
    expect(Laatimisvaiheet.filterByVersion(2013, allLaatimisvaiheet)).toEqual(
      []
    );
  });
});
