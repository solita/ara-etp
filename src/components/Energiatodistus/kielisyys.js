import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';

const kielisyydet = ['fi', 'sv', 'bilingual'];

export const kieli = R.compose(R.map(parseInt), R.invertObj)(kielisyydet);
export const kieliKey = id => kielisyydet[id];

const isKielisyys = kieliId =>
  R.compose(
    Maybe.isSome,
    R.filter(R.equals(kieliId)),
    R.path(['perustiedot', 'kieli'])
  );

export const onlyFi = isKielisyys(0);
export const onlySv = isKielisyys(1);
export const bilingual = isKielisyys(2);

export const fi = R.anyPass([onlyFi, bilingual]);
export const sv = R.anyPass([onlySv, bilingual]);
