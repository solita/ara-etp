import Input from './Input';
import * as R from 'ramda';

export default { title: 'Input' };

const defaultProps = {
  id: 'prefilledid',
  name: 'prefilledname',
  label: 'Pre-filled label',
  model: 'Pre-filled value'
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

export const numberInput = () => {
  let value = 69;
  return {
    Component: Input,
    props: {
      id: 'prefilledid',
      name: 'number-input',
      label: 'Number input',
      model: value,
      type: 'number'
    }
  };
};
