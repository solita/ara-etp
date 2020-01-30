import Alert from './Alert.svelte';

export default { title: 'Alert' };

export const warnWithText = () => ({
  Component: Alert,
  props: {
    type: 'warn',
    text: 'Varoitus'
  }
});

export const errorWithText = () => ({
  Component: Alert,
  props: {
    type: 'error',
    text: 'Virhe'
  }
});

export const successWithText = () => ({
  Component: Alert,
  props: {
    type: 'suceess',
    text: 'Onnistu'
  }
});