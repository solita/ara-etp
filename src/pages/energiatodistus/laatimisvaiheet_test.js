import { assert } from 'chai';
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
    assert.equal(
      Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(2)),
      true
    );

    assert.equal(
      Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(1)),
      false
    );

    assert.equal(
      Laatimisvaiheet.isOlemassaOlevaRakennus(et2018inVaihe(0)),
      false
    );

    assert.equal(
      Laatimisvaiheet.isOlemassaOlevaRakennus(et2018UnknownVaihe),
      false
    );
  });

  it('Olemassaoleva rakennus - 2013', () => {
    assert.equal(Laatimisvaiheet.isOlemassaOlevaRakennus(et2013(false)), true);

    assert.equal(Laatimisvaiheet.isOlemassaOlevaRakennus(et2013(true)), false);
  });
});
