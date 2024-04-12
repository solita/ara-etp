import Header from './Header';
import HeaderDecorator from '@Pages/header/HeaderDecorator.svelte';

export default {
  title: 'Header',
  // Add background to the Header, as its contents are only in white
  decorators: [() => HeaderDecorator]
};

export const withText = () => ({
  Component: Header,
  props: {}
});
