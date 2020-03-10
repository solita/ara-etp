import Link from './Link';

export default { title: 'Link' };

export const withDefault = () => ({
  Component: Link,
  props: {
    href: 'http://example.com',
    text: 'To Example.com'
  }
});
