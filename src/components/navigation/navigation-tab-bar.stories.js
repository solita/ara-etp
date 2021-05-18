import NavigationTabBar from './navigation-tab-bar';

export default { title: 'NavigationTabBar' };

export const withMultipleLinks = () => ({
  Component: NavigationTabBar,
  props: {
    links: [
      { text: 'link1' },
      { text: 'link2' },
      { text: 'link3' },
      { text: 'link4' },
      { text: 'link5' },
      { text: 'link6' }
    ]
  }
});
