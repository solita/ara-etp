import Litepicker from 'litepicker';

export const litepicker = (node, options) => {
  const picker = new Litepicker({ ...options, element: node });

  return () => picker.destroy();
};
