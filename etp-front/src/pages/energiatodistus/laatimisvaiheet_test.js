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
