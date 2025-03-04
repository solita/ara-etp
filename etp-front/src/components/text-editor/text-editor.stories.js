import TextEditor from './text-editor.svelte';

export default { title: 'TextEditor' };

export const withDefaults = () => ({
  Component: TextEditor,
  props: {
    id: 'test',
    name: 'test',
    label: 'test',
    required: true
  }
});
