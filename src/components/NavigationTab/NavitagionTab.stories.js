import NavigationTab from './NavigationTab';

export default { title: 'NavigationTab' };

export const withText = () => ({
  Component: NavigationTab,
  props: {
    text: 'Link'
  }
});
