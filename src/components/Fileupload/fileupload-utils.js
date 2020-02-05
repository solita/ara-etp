import * as R from 'ramda';
import * as RamdaUtils from '../../utils/ramda-utils';
import * as Either from '../../utils/either-utils';

export const isValidAmountFiles = R.curry((multiple, files) => {
  if (multiple) return R.compose(R.lte(0), R.length)(files);
  return R.compose(RamdaUtils.inRangeInclusive(0, 1), R.length)(files);
});

export const filesError = R.curry((predicate, errorState, files) =>
  R.ifElse(predicate, Either.Right, R.always(Either.Left(errorState)))(files)
);

export const validFilesInput = R.curry((stateTransitions, files) =>
  R.reduce(
    (acc, stateTransition) => R.chain(stateTransition, acc),
    Either.of(files),
    stateTransitions
  )
);
