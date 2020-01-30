import * as R from 'ramda';
import * as Either from '../../utils/either-utils';

export const isValidAmountFiles = R.curry((multiple, files) => {
  if (multiple) return R.length(files) >= 0;
  return 1 >= R.length(files) && R.length(files) >= 0;
});

export const fileuploadError = R.curry((multiple, state) =>
  R.compose(
    R.ifElse(R.chain(isValidAmountFiles(multiple)), R.identity, Either.Left),
    Either.of,
    R.prop('files')
  )(state)
);
