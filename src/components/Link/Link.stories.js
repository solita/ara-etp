import Link from './Link';

export default { title: 'Link' };

export const withText = () => ({
  Component: Link,
  props: {
    href: 'http://example.com',
    text: 'To Example.com'
  }
});

export const withIcon = () => ({
  Component: Link,
  props: {
    href: 'http://example.com',
    icon: 'home'
  }
});

export const withTextAndIcon = () => ({
  Component: Link,
  props: {
    href: 'http://example.com',
    icon: 'home',
    text: 'To example.com'
  }
});
