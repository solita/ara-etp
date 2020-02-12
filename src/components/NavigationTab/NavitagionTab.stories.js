import NavigationTab from './NavigationTab.svelte';

export default { title: 'NavigationTab' };

export const withText = () => ({
  Component: NavigationTab,
  props: {
    text: 'Link'
  }
});
