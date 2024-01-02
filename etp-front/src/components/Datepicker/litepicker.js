import Litepicker from 'litepicker';

export const litepicker = (node, opts) => {
  const input = node.querySelector('input');

  const options = {
    element: input,
    format: 'D.M.YYYY',
    singleMode: true,
    lang: opts.lang,
    dropdowns: {
      minYear: 2013,
      maxYear: new Date().getFullYear() + 11,
      months: true,
      years: true
    },
    onSelect: opts.update
  };

  const picker = new Litepicker(options);

  return () => picker.destroy();
};
