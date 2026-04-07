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

const allLaatimisvaiheet = [
  {
    id: 0,
    'label-fi': 'Rakennuslupavaihe, uudisrakennus',
    'label-sv': 'Bygglov'
  },
  {
    id: 1,
    'label-fi': 'Käyttöönottovaihe, uudisrakennus',
    'label-sv': 'Införandet'
  },
  {
    id: 2,
    'label-fi': 'Olemassa oleva rakennus',
    'label-sv': 'Befintlig byggnad'
  },
  {
    id: 3,
    'label-fi': 'Rakennuslupavaihe, laajamittainen perusparannus',
    'label-sv': 'Rakennuslupavaihe, laajamittainen perusparannus (sv)'
  },
  {
    id: 4,
    'label-fi': 'Käyttöönottovaihe, laajamittainen perusparannus',
    'label-sv': 'Käyttöönottovaihe, laajamittainen perusparannus (sv)'
  }
];

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

describe('filterByVersion: ', () => {
  it('given version 2018 and all laatimisvaiheet, when filtering, then returns only ids 0, 1, 2', () => {
    // given
    const version = 2018;

    // when
    const result = Laatimisvaiheet.filterByVersion(version, allLaatimisvaiheet);

    // then
    expect(result).toEqual([
      allLaatimisvaiheet[0],
      allLaatimisvaiheet[1],
      allLaatimisvaiheet[2]
    ]);
  });

  it('given version 2026 and all laatimisvaiheet, when filtering, then returns all ids 0, 1, 2, 3, 4', () => {
    // given
    const version = 2026;

    // when
    const result = Laatimisvaiheet.filterByVersion(version, allLaatimisvaiheet);

    // then
    expect(result).toEqual(allLaatimisvaiheet);
  });

  it('given version 2018 and empty laatimisvaiheet, when filtering, then returns empty array', () => {
    // given
    const version = 2018;

    // when
    const result = Laatimisvaiheet.filterByVersion(version, []);

    // then
    expect(result).toEqual([]);
  });

  it('given version 2013, when filtering, then returns empty array', () => {
    // given
    const version = 2013;

    // when
    const result = Laatimisvaiheet.filterByVersion(version, allLaatimisvaiheet);

    // then
    expect(result).toEqual([]);
  });

  it('given version 2018 and partial laatimisvaiheet with ids 0 and 3, when filtering, then returns only id 0', () => {
    // given
    const version = 2018;
    const partial = [allLaatimisvaiheet[0], allLaatimisvaiheet[3]];

    // when
    const result = Laatimisvaiheet.filterByVersion(version, partial);

    // then
    expect(result).toEqual([allLaatimisvaiheet[0]]);
  });

  it('given version 2018, when filtering, then returned objects preserve all original properties', () => {
    // given
    const version = 2018;

    // when
    const result = Laatimisvaiheet.filterByVersion(version, allLaatimisvaiheet);

    // then
    expect(result[0]).toHaveProperty('id', 0);
    expect(result[0]).toHaveProperty(
      'label-fi',
      'Rakennuslupavaihe, uudisrakennus'
    );
    expect(result[0]).toHaveProperty('label-sv', 'Bygglov');
  });
});
