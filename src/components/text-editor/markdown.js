import * as R from 'ramda';

export const toPlainText = R.compose(
  R.join(''),
  R.filter(R.test(/(\w|[ÅÄÖ])/i)),
  R.replace(/<\/?[^>]+(>|$)/g, ''))

export const plainTextValidator =
  R.over(R.lensProp('predicate'), predicate => R.compose(predicate, toPlainText));