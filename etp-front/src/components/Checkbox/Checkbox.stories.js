import Checkbox from './Checkbox';

export default { title: 'Checkbox' };

export const withLabel = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox' }
});

export const withLabelDisabled = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox', disabled: true }
});

export const withLabelAndChecked = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox', model: true }
});

export const withLabelAndCheckedDisabled = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox', model: true, disabled: true }
});
