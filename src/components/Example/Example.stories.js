import Example from './Example';

export default { title: 'Example' };

export const withText = () => ({
  Component: Example,
  props: {
    text: 'Example-component'
  }
});
