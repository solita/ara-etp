import * as R from 'ramda';
import HierarchicalDropdownList from './HierarchicalDropdownList';

export default { title: 'HierarchicalDropdownList' };

export const withItems = () => ({
  Component: HierarchicalDropdownList,
  props: {
    items: [
      {
        label: 'item1',
        leafs: [
          { label: 'subitem1' },
          {
            label: 'subitem2',
            leafs: [
              { label: 'subitem21' },
              { label: 'subitem22' },
              { label: 'subitem23' }
            ]
          }
        ]
      },
      {
        label: 'item2',
        leafs: [
          { label: 'subitem1' },
          {
            label: 'subitem2',
            leafs: [
              { label: 'subitem21' },
              { label: 'subitem22' },
              { label: 'subitem23' }
            ]
          }
        ]
      }
    ],
    labelLens: R.lensProp('label'),
    leafsLens: R.lensProp('leafs')
  }
});
