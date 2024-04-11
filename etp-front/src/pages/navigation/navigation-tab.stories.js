import NavigationTab from './navigation-tab';

export default { title: 'NavigationTab' };

export const withText = () => ({
  Component: NavigationTab,
  props: {
    label: 'Link',
    href: '/'
  }
});
