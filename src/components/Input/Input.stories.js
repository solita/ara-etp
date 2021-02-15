import Input from './Input';
import * as R from 'ramda';

export default { title: 'Input' };

const defaultProps = {
  id: 'prefilledid',
  name: 'prefilledname',
  label: 'Pre-filled label',
  value: 'Pre-filled value'
};

export const withText = () => ({
  Component: Input,
  props: defaultProps
});

export const withRequired = () => ({
  Component: Input,
  props: R.assoc('required', true, defaultProps)
});

export const withCaret = () => ({
  Component: Input,
  props: R.assoc('caret', true, defaultProps)
});

export const withFailedValidation = () => ({
  Component: Input,
  props: R.assoc('validation', R.always(false), defaultProps)
});
