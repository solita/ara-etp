import Button from './Button.svelte';

export default { title: 'Button' };

export const withTextPrimary = () => ({
  Component: Button,
  props: {
    text: 'Primary-Button',
    type: 'primary'
  }
});

export const withTextSecondary = () => ({
  Component: Button,
  props: {
    text: 'Secondary-Button',
    type: 'secondary'
  }
});

export const withTextError = () => ({
  Component: Button,
  props: {
    text: 'Error-Button',
    type: 'error'
  }
});
