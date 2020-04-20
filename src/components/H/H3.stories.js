import H3 from './H3';

export default { title: 'H3' };

export const withText = () => ({
  Component: H3,
  props: {
    text: 'H3-component'
  }
});
