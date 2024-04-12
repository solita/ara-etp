import Link from './Link';
import * as Maybe from '@Utility/maybe-utils';

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
    icon: Maybe.Some('home')
  }
});

export const withTextAndIcon = () => ({
  Component: Link,
  props: {
    href: 'http://example.com',
    icon: Maybe.Some('home'),
    text: 'To example.com'
  }
});
