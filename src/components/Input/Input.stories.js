import Input from './Input.svelte';

export default { title: 'Input' };

export const withText = () => ({
  Component: Input,
  props: {
    label: 'Pre-filled label',
    value: 'Pre-filled value',
    id: 'prefilledid'
  }
});

export const withRequired = () => ({
  Component: Input,
  props: {
    label: 'Pre-filled label',
    value: 'Pre-filled value',
    id: 'prefilledid',
    required: true
  }
});

export const withError = () => ({
  Component: Input,
  props: {
    value: 'Pre-filled value',
    label: 'Pre-filled label',
    id: 'prefilledid',
    error: true
  }
});

export const withCaret = () => ({
  Component: Input,
  props: {
    value: 'Pre-filled value',
    label: 'Pre-filled label',
    id: 'prefilledid',
    caret: true
  }
});

export const errorWithCaret = () => ({
  Component: Input,
  props: {
    value: 'Pre-filled value',
    label: 'Pre-filled label',
    id: 'prefilledid',
    error: true,
    caret: true
  }
});
