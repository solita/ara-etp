import Radio from './Radio';

export default { title: 'Radio' };

export const withLabel = () => ({
  Component: Radio,
  props: { label: 'Radio', group: 0, value: 1 }
});

export const withLabelDisabled = () => ({
  Component: Radio,
  props: { label: 'Radio', disabled: true, group: 0, value: 1 }
});

export const withSelected = () => ({
  Component: Radio,
  props: { label: 'Radio', group: 0, value: 0 }
});

export const withSelectedDisabled = () => ({
  Component: Radio,
  props: { label: 'Radio', disabled: true, group: 0, value: 0 }
});
