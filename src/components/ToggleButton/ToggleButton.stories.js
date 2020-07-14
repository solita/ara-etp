import ToggleButton from './ToggleButton';

export default { title: 'ToggleButton' };

export const withLabel = () => ({
  Component: ToggleButton,
  props: { label: 'ToggleButton' }
});

export const withCustomIcons = () => ({
  Component: ToggleButton,
  props: {
    label: 'ToggleButton',
    toggledIcon: 'arrow_back',
    notToggledIcon: 'arrow_forward'
  }
});
