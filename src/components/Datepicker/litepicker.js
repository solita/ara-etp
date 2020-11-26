import Litepicker from 'litepicker';

export const litepicker = (node, opts) => {
  const input = node.querySelector('input');

  const options = {
    element: input,
    format: 'D.M.YYYY',
    singleMode: true,
    lang: opts.lang,
    dropdowns: { minYear: 1970, maxYear: null, months: true, years: true },
    onSelect: opts.update
  };

  const picker = new Litepicker(options);

  return () => picker.destroy();
};
