import DropdownList from './DropdownList';

export default { title: 'DropdownList' };

export const withItems = () => ({
  Component: DropdownList,
  props: {
    items: [
      'dropitem1',
      'dropitem2',
      'dropitem1',
      'dropitem2',
      'dropitem1',
      'dropitem2'
    ]
  }
});
