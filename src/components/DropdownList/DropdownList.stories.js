import DropdownList from './DropdownList.svelte';

export default { title: 'DropdownList' };

export const withItems = () => ({
  Component: DropdownList,
  props: {
    state: [
      'dropitem1',
      'dropitem2',
      'dropitem1',
      'dropitem2',
      'dropitem1',
      'dropitem2'
    ]
  }
});
