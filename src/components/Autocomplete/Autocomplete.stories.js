import Autocomplete from './Autocomplete.svelte';

export default { title: 'Autocomplete' };

export const withItems = () => ({
  Component: Autocomplete,
  props: {
    state: {
      input: {
        value: 'Pre-filled value',
        label: 'Pre-filled label',
        caret: true,
        id: 'storybook-test-id'
      },
      dropdown: ['dropitem1', 'dropitem2', 'dropitem3']
    }
  }
});
