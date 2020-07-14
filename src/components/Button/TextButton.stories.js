import TextButton from './TextButton';

export default { title: 'TextButton' };

export const withIcon = () => ({
  Component: TextButton,
  props: {
    text: 'Primary-TextButton',
    icon: 'add'
  }
});
