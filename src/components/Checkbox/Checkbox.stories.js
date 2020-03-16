import Checkbox from './Checkbox';

export default { title: 'Checkbox' };

export const withLabel = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox' }
});

export const withLabelAndChecked = () => ({
  Component: Checkbox,
  props: { label: 'Checkbox', checked: true }
});
