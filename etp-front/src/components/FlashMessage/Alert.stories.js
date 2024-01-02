import Alert from './Alert';

export default { title: 'Alert' };

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
    type: 'success',
    text: 'Onnistu'
  }
});
