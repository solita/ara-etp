import Spinner from './Spinner';

export default { title: 'Spinner' };

export const withDefaults = () => ({
  Component: Spinner,
  // Animation disabled to ensure visual tests always match
  props: { disableAnimation: true }
});
