import Example from './Example.svelte';

export default { title: 'Example' };

export const withText = () => ({
  Component: Example,
  props: {
    text:'Example-component'
  }
});