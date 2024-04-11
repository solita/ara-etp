import Footer from './Footer';

export default { title: 'Footer' };

export const withText = () => ({
  Component: Footer,
  props: { version: { version: 27 } }
});
