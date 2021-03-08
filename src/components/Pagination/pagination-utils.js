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

export const truncate = (numberOfPages, page) => {
  const pages = R.range(0, numberOfPages);
  const heads = R.slice(0, 2, pages);
  const tails = R.compose(R.reverse, R.slice(0, 2), R.reverse)(pages);

  const nearPages = R.slice(page - 1, page + 2, pages);

  const truncated = R.uniq([...heads, ...nearPages, ...tails]);

  return truncated;
};
