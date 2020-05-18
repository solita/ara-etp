import Tooltip from './Tooltip.svelte';
export default { title: 'Tooltip' };

export const primary = () => ({
  Component: Tooltip,
  props: { text: 'Tooltip text' }
});

export const secondary = () => ({
  Component: Tooltip,
  props: { text: 'Tooltip text', style: 'secondary' }
});

export const withLeftTop = () => ({
  Component: Tooltip,
  props: { vertical: 'top', horizontal: 'left', text: 'Tooltip text' }
});

export const withRightTop = () => ({
  Component: Tooltip,
  props: { vertical: 'top', horizontal: 'right', text: 'Tooltip text' }
});

export const withLeftBottom = () => ({
  Component: Tooltip,
  props: { vertical: 'bottom', horizontal: 'left', text: 'Tooltip text' }
});

export const withRightBottom = () => ({
  Component: Tooltip,
  props: { vertical: 'bottom', horizontal: 'right', text: 'Tooltip text' }
});
