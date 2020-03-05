import * as R from 'ramda';

export const consecutivePairsForPageCount = R.compose(
  R.aperture(2),
  R.range(1),
  R.inc
);

export const isPairWithCurrentPage = R.curry((current, pagePair) =>
  R.either(
    R.compose(R.equals(current), R.head),
    R.compose(R.equals(current), R.last)
  )(pagePair)
);

export const combinePairs = R.compose(R.uniq, R.reduce(R.concat, []));

export const dropFirstAndLastPages = R.curry((pageCount, pages) =>
  R.reject(R.either(R.equals(1), R.equals(pageCount)), pages)
);

export const nearForCurrent = R.curry((pageCount, current) =>
  R.compose(
    dropFirstAndLastPages(pageCount),
    combinePairs,
    R.filter(isPairWithCurrentPage(current)),
    consecutivePairsForPageCount
  )(pageCount)
);
