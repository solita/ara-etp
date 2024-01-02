import H2 from './H2';

export default { title: 'H2' };

export const withText = () => ({
  Component: H2,
  props: {
    text: 'H2-component'
  }
});
