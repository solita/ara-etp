import H1 from './H1.svelte';

export default { title: 'H1' };

export const withText = () => ({
  Component: H1,
  props: {
    text: 'H1-component'
  }
});
